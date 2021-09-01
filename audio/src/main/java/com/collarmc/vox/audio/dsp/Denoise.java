package com.collarmc.vox.audio.dsp;

import com.collarmc.vox.jna.CLibrary;
import com.collarmc.vox.jna.LibraryLoader;
import com.google.common.io.ByteStreams;
import com.sun.jna.Library;
import com.sun.jna.ptr.PointerByReference;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public final class Denoise implements Closeable {

    private final RNNoiseNative noise;
    private final RNNModel model;
    private final DenoiseState state;

    public Denoise() {
        this.noise = LibraryLoader.load("librnnoise", RNNoiseNative.class);
        File path = unpackModel();
        CLibrary.FILE file = CLibrary.INSTANCE.fopen(path.getAbsolutePath(), "r");
        try {
            model = noise.rnnoise_model_from_file(file);
        } finally {
            CLibrary.INSTANCE.fclose(file);
        }
        state = noise.rnnoise_create(model);
        noise.rnnoise_init(state, model);
    }

    /**
     * @return frame size
     */
    public int getFrameSize() {
        return noise.rnnoise_get_frame_size();
    }

    /**
     * Denoises the buffer
     * @param buff to denoise
     * @return frame
     */
    public byte[] denoiseBuffer(byte[] buff) {
        int denoiseFrameSize = getFrameSize();
        for (int i = 0; i < buff.length; i+= denoiseFrameSize) {
            byte[] frame = new byte[denoiseFrameSize];
            // Copy frame from audio buffer
            System.arraycopy(buff, 0, frame, 0, denoiseFrameSize);
            // Denoise the frame
            byte[] processedFrame = denoiseFrame(frame);
            // Copy back to buffer
            System.arraycopy(processedFrame, 0, buff, i, i + denoiseFrameSize);
        }
        return buff;
    }

    private byte[] denoiseFrame(byte[] frame) {
        ByteBuffer frameBuffer = ByteBuffer.allocateDirect(frame.length);
        frameBuffer.put(frame);
        frameBuffer.flip();
        ByteBuffer resultBuffer = ByteBuffer.allocateDirect(noise.rnnoise_get_frame_size() * 4);
        noise.rnnoise_process_frame(state, resultBuffer.asFloatBuffer(), frameBuffer.asFloatBuffer());
        byte[] bytes = new byte[resultBuffer.position()];
        resultBuffer.get(bytes);
        return bytes;
    }

    @Override
    public void close() throws IOException {
        this.noise.rnnoise_destroy(this.state);
        this.noise.rnnoise_model_free(model);
    }

    /**
     * Native interface for rnnoise
     */
    interface RNNoiseNative extends Library {
        int rnnoise_get_frame_size();
        int rnnoise_init(DenoiseState st, RNNModel model);
        DenoiseState rnnoise_create(RNNModel  model);
        void rnnoise_destroy(DenoiseState st);
        float rnnoise_process_frame(DenoiseState st, FloatBuffer out, FloatBuffer in);
        RNNModel rnnoise_model_from_file(CLibrary.FILE file);
        void rnnoise_model_free(RNNModel model);
    }

    static class DenoiseState extends PointerByReference {}

    static class RNNModel extends PointerByReference {}

    private static File unpackModel() {
        File model;
        try {
            model = File.createTempFile("model", ".rnnn");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (InputStream is = LibraryLoader.class.getResourceAsStream("/noise/lq.rnnn");
             FileOutputStream os = new FileOutputStream(model)) {
            if (is == null) {
                throw new IllegalStateException("could not find model");
            }
            ByteStreams.copy(is, os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return model;
    }
}
