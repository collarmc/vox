package com.collarmc.vox.audio.dsp;

import com.collarmc.vox.jna.LibraryLoader;
import com.sun.jna.Library;
import com.sun.jna.ptr.PointerByReference;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * Echo canceller based on speexdsp
 */
public final class EchoCanceller implements Closeable {

    private final SpeexDSP dsp;
    private final SpeexEchoState st;

    public EchoCanceller(int frameSize, int filterLength, int micCount, int speakerCount) {
        this.dsp = LibraryLoader.load("libspeexdsp", SpeexDSP.class);
        this.st = dsp.speex_echo_state_init_mc(frameSize, filterLength, micCount, speakerCount);
    }

    /**
     * Cancels echos on microphone input based on sound queued to the card using {@link #captureOutputToSoundCard(byte[])}
     * @param frame from mic
     * @return processed frame
     */
    public byte[] processMicrophoneInput(byte[] frame) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(frame.length);
        buffer.put(frame);
        buffer.flip();
        ByteBuffer output = ByteBuffer.allocateDirect(frame.length);
        dsp.speex_echo_capture(st, buffer.asShortBuffer(), output.asShortBuffer());
        byte[] out = new byte[frame.length];
        output.flip();
        output.get(out);
        return out;
    }

    /**
     * Capture output queued to the sound card
     * @param frame frame that was queued to sound card
     */
    public void captureOutputToSoundCard(byte[] frame) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(frame.length);
        buffer.put(frame);
        buffer.flip();
        dsp.speex_echo_playback(st, buffer.asShortBuffer());
    }

    @Override
    public void close() throws IOException {
        dsp.speex_echo_state_destroy(st);
    }

    interface SpeexDSP extends Library {
        int SPEEX_ECHO_GET_FRAME_SIZE = 3;

        /** Set sampling rate */
        int SPEEX_ECHO_SET_SAMPLING_RATE = 24;
        int SPEEX_ECHO_GET_SAMPLING_RATE = 25;

        /** Get size of impulse response (int32) */
        int SPEEX_ECHO_GET_IMPULSE_RESPONSE_SIZE = 27;

        /* Can't set window content */
        /** Get impulse response (int32[]) */
        int SPEEX_ECHO_GET_IMPULSE_RESPONSE = 29;

        /** Creates a new echo canceller state
         * @param frame_size Number of samples to process at one time (should correspond to 10-20 ms)
         * @param filter_length Number of samples of echo to cancel (should generally correspond to 100-500 ms)
         * @return Newly-created echo canceller state
         */
        SpeexEchoState speex_echo_state_init(int frame_size, int filter_length);

        /**
         * Creates a new multi-channel echo canceller state
         * @param frame_size Number of samples to process at one time (should correspond to 10-20 ms)
         * @param filter_length Number of samples of echo to cancel (should generally correspond to 100-500 ms)
         * @param nb_mic Number of microphone channels
         * @param nb_speakers Number of speaker channels
         * @return Newly-created echo canceller state
         */
        SpeexEchoState speex_echo_state_init_mc(int frame_size, int filter_length, int nb_mic, int nb_speakers);

        /**
         * Destroys an echo canceller state
         * @param st Echo canceller state
         */
        void speex_echo_state_destroy(SpeexEchoState st);

        /**
         * Performs echo cancellation a frame, based on the audio sent to the speaker (no delay is added
         * to playback in this form)
         *
         * @param st Echo canceller state
         * @param rec Signal from the microphone (near end + far end echo)
         * @param play Signal played to the speaker (received from far end)
         * @param out Returns near-end signal with echo removed
         */
        void speex_echo_cancellation(SpeexEchoState st, ShortBuffer rec, ShortBuffer play, ShortBuffer out);

        /**
         * Perform echo cancellation using internal playback buffer, which is delayed by two frames
         * to account for the delay introduced by most soundcards (but it could be off!)
         * @param st Echo canceller state
         * @param rec Signal from the microphone (near end + far end echo)
         * @param out Returns near-end signal with echo removed
         */
        void speex_echo_capture(SpeexEchoState st, ShortBuffer rec, ShortBuffer out);

        /**
         * Let the echo canceller know that a frame was just queued to the soundcard
         * @param st Echo canceller state
         * @param play Signal played to the speaker (received from far end)
         */
        void speex_echo_playback(SpeexEchoState st, ShortBuffer play);

        /**
         * Reset the echo canceller to its original state
         * @param st Echo canceller state
         */
        void speex_echo_state_reset(SpeexEchoState st);

        /** Used like the ioctl function to control the echo canceller parameters
         *
         * @param st Echo canceller state
         * @param request ioctl-type request (one of the SPEEX_ECHO_* macros)
         * @param ptr Data exchanged to-from function
         * @return 0 if no error, -1 if request in unknown
         */
        int speex_echo_ctl(SpeexEchoState st, int request, PointerByReference ptr);
    }

    public static class SpeexEchoState extends PointerByReference {}
}
