package team.catgirl.vox.audio.opus;

import club.minnced.opus.util.OpusLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;
import team.catgirl.vox.audio.AudioException;
import tomp2p.opuswrapper.Opus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Logger;

public final class OpusCodec {

    private static final Logger LOGGER = Logger.getLogger(OpusCodec.class.getName());

    private final Opus codec;

    public OpusCodec() {
        try {
            boolean printVersion = !OpusLibrary.isInitialized();
            OpusLibrary.loadFromJar();
            if (printVersion) {
                LOGGER.info("Opus version " + Opus.INSTANCE.opus_get_version_string());
            }
        } catch (IOException e) {
            throw new IllegalStateException("could not load opus", e);
        }
        this.codec = Opus.INSTANCE;
    }

    public int opus_encoder_get_size(int channels) {
        return codec.opus_encoder_get_size(channels);
    }

    public PointerByReference opus_encoder_create(int Fs, int channels, int application, IntBuffer error) {
        return codec.opus_encoder_create(Fs, channels, application, error);
    }

    public int opus_encoder_init(PointerByReference st, int Fs, int channels, int application) {
        return codec.opus_encoder_init(st, Fs, channels, application);
    }

    public int opus_encode(PointerByReference st, ShortBuffer pcm, int frame_size, ByteBuffer data, int max_data_bytes) {
        return codec.opus_encode(st, pcm, frame_size, data, max_data_bytes);
    }

    public int opus_encode(PointerByReference st, ShortByReference pcm, int frame_size, Pointer data, int max_data_bytes) {
        return codec.opus_encode(st, pcm, frame_size, data, max_data_bytes);
    }

    public int opus_encode_float(PointerByReference st, float[] pcm, int frame_size, ByteBuffer data, int max_data_bytes) {
        return codec.opus_encode_float(st, pcm, frame_size, data, max_data_bytes);
    }

    public int opus_encode_float(PointerByReference st, FloatByReference pcm, int frame_size, Pointer data, int max_data_bytes) {
        return codec.opus_encode_float(st, pcm, frame_size, data, max_data_bytes);
    }

    public void opus_encoder_destroy(PointerByReference st) {
        codec.opus_encoder_destroy(st);
    }

    public int opus_encoder_ctl(PointerByReference st, int request, Object... varargs) {
        return codec.opus_encoder_ctl(st, request, varargs);
    }

    public int opus_decoder_get_size(int channels) {
        return codec.opus_decoder_get_size(channels);
    }

    public PointerByReference opus_decoder_create(int Fs, int channels, IntBuffer error) {
        return codec.opus_decoder_create(Fs, channels, error);
    }

    public int opus_decoder_init(PointerByReference st, int Fs, int channels) {
        return codec.opus_decoder_init(st, Fs, channels);
    }

    public int opus_decode(PointerByReference st, byte[] data, int len, ShortBuffer pcm, int frame_size, int decode_fec) {
        return codec.opus_decode(st, data, len, pcm, frame_size, decode_fec);
    }

    public int opus_decode(PointerByReference st, Pointer data, int len, ShortByReference pcm, int frame_size, int decode_fec) {
        return codec.opus_decode(st, data, len, pcm, frame_size, decode_fec);
    }

    public int opus_decode_float(PointerByReference st, byte[] data, int len, FloatBuffer pcm, int frame_size, int decode_fec) {
        return codec.opus_decode_float(st, data, len, pcm, frame_size, decode_fec);
    }

    public int opus_decode_float(PointerByReference st, Pointer data, int len, FloatByReference pcm, int frame_size, int decode_fec) {
        return codec.opus_decode_float(st, data, len, pcm, frame_size, decode_fec);
    }

    public int opus_decoder_ctl(PointerByReference st, int request, Object... varargs) {
        return codec.opus_decoder_ctl(st, request, varargs);
    }

    public void opus_decoder_destroy(PointerByReference st) {
        codec.opus_decoder_destroy(st);
    }

    public int opus_packet_parse(byte[] data, int len, ByteBuffer out_toc, byte[] frames, ShortBuffer size, IntBuffer payload_offset) {
        return codec.opus_packet_parse(data, len, out_toc, frames, size, payload_offset);
    }

    public int opus_packet_get_bandwidth(byte[] data) {
        return codec.opus_packet_get_bandwidth(data);
    }

    public int opus_packet_get_samples_per_frame(byte[] data, int Fs) {
        return codec.opus_packet_get_samples_per_frame(data, Fs);
    }

    public int opus_packet_get_nb_channels(byte[] data) {
        return codec.opus_packet_get_nb_channels(data);
    }

    public int opus_packet_get_nb_frames(byte[] packet, int len) {
        return codec.opus_packet_get_nb_frames(packet, len);
    }

    public int opus_packet_get_nb_samples(byte[] packet, int len, int Fs) {
        return codec.opus_packet_get_nb_samples(packet, len, Fs);
    }

    public int opus_decoder_get_nb_samples(PointerByReference dec, byte[] packet, int len) {
        return codec.opus_decoder_get_nb_samples(dec, packet, len);
    }

    public int opus_decoder_get_nb_samples(PointerByReference dec, Pointer packet, int len) {
        return codec.opus_decoder_get_nb_samples(dec, packet, len);
    }

    public void opus_pcm_soft_clip(FloatBuffer pcm, int frame_size, int channels, FloatBuffer softclip_mem) {
        codec.opus_pcm_soft_clip(pcm, frame_size, channels, softclip_mem);
    }

    public int opus_repacketizer_get_size() {
        return codec.opus_repacketizer_get_size();
    }

    public PointerByReference opus_repacketizer_init(PointerByReference rp) {
        return codec.opus_repacketizer_init(rp);
    }

    public PointerByReference opus_repacketizer_create() {
        return codec.opus_repacketizer_create();
    }

    public void opus_repacketizer_destroy(PointerByReference rp) {
        codec.opus_repacketizer_destroy(rp);
    }

    public int opus_repacketizer_cat(PointerByReference rp, byte[] data, int len) {
        return codec.opus_repacketizer_cat(rp, data, len);
    }

    public int opus_repacketizer_cat(PointerByReference rp, Pointer data, int len) {
        return codec.opus_repacketizer_cat(rp, data, len);
    }

    public int opus_repacketizer_out_range(PointerByReference rp, int begin, int end, ByteBuffer data, int maxlen) {
        return codec.opus_repacketizer_out_range(rp, begin, end, data, maxlen);
    }

    public int opus_repacketizer_out_range(PointerByReference rp, int begin, int end, Pointer data, int maxlen) {
        return codec.opus_repacketizer_out_range(rp, begin, end, data, maxlen);
    }

    public int opus_repacketizer_get_nb_frames(PointerByReference rp) {
        return codec.opus_repacketizer_get_nb_frames(rp);
    }

    public int opus_repacketizer_out(PointerByReference rp, ByteBuffer data, int maxlen) {
        return codec.opus_repacketizer_out(rp, data, maxlen);
    }

    public int opus_repacketizer_out(PointerByReference rp, Pointer data, int maxlen) {
        return codec.opus_repacketizer_out(rp, data, maxlen);
    }

    public int opus_packet_pad(ByteBuffer data, int len, int new_len) {
        return codec.opus_packet_pad(data, len, new_len);
    }

    public int opus_packet_unpad(ByteBuffer data, int len) {
        return codec.opus_packet_unpad(data, len);
    }

    public int opus_multistream_packet_pad(ByteBuffer data, int len, int new_len, int nb_streams) {
        return codec.opus_multistream_packet_pad(data, len, new_len, nb_streams);
    }

    public int opus_multistream_packet_unpad(ByteBuffer data, int len, int nb_streams) {
        return codec.opus_multistream_packet_unpad(data, len, nb_streams);
    }

    public String opus_strerror(int error) {
        return codec.opus_strerror(error);
    }

    public String opus_get_version_string() {
        return codec.opus_get_version_string();
    }

    public int opus_multistream_encoder_get_size(int streams, int coupled_streams) {
        return codec.opus_multistream_encoder_get_size(streams, coupled_streams);
    }

    public int opus_multistream_surround_encoder_get_size(int channels, int mapping_family) {
        return codec.opus_multistream_surround_encoder_get_size(channels, mapping_family);
    }

    public PointerByReference opus_multistream_encoder_create(int Fs, int channels, int streams, int coupled_streams, byte[] mapping, int application, IntBuffer error) {
        return codec.opus_multistream_encoder_create(Fs, channels, streams, coupled_streams, mapping, application, error);
    }

    public PointerByReference opus_multistream_surround_encoder_create(int Fs, int channels, int mapping_family, IntBuffer streams, IntBuffer coupled_streams, ByteBuffer mapping, int application, IntBuffer error) {
        return codec.opus_multistream_surround_encoder_create(Fs, channels, mapping_family, streams, coupled_streams, mapping, application, error);
    }

    public int opus_multistream_encoder_init(PointerByReference st, int Fs, int channels, int streams, int coupled_streams, byte[] mapping, int application) {
        return codec.opus_multistream_encoder_init(st, Fs, channels, streams, coupled_streams, mapping, application);
    }

    public int opus_multistream_encoder_init(PointerByReference st, int Fs, int channels, int streams, int coupled_streams, Pointer mapping, int application) {
        return codec.opus_multistream_encoder_init(st, Fs, channels, streams, coupled_streams, mapping, application);
    }

    public int opus_multistream_surround_encoder_init(PointerByReference st, int Fs, int channels, int mapping_family, IntBuffer streams, IntBuffer coupled_streams, ByteBuffer mapping, int application) {
        return codec.opus_multistream_surround_encoder_init(st, Fs, channels, mapping_family, streams, coupled_streams, mapping, application);
    }

    public int opus_multistream_surround_encoder_init(PointerByReference st, int Fs, int channels, int mapping_family, IntByReference streams, IntByReference coupled_streams, Pointer mapping, int application) {
        return codec.opus_multistream_surround_encoder_init(st, Fs, channels, mapping_family, streams, coupled_streams, mapping, application);
    }

    public int opus_multistream_encode(PointerByReference st, ShortBuffer pcm, int frame_size, ByteBuffer data, int max_data_bytes) {
        return codec.opus_multistream_encode(st, pcm, frame_size, data, max_data_bytes);
    }

    public int opus_multistream_encode(PointerByReference st, ShortByReference pcm, int frame_size, Pointer data, int max_data_bytes) {
        return codec.opus_multistream_encode(st, pcm, frame_size, data, max_data_bytes);
    }

    public int opus_multistream_encode_float(PointerByReference st, float[] pcm, int frame_size, ByteBuffer data, int max_data_bytes) {
        return codec.opus_multistream_encode_float(st, pcm, frame_size, data, max_data_bytes);
    }

    public int opus_multistream_encode_float(PointerByReference st, FloatByReference pcm, int frame_size, Pointer data, int max_data_bytes) {
        return codec.opus_multistream_encode_float(st, pcm, frame_size, data, max_data_bytes);
    }

    public void opus_multistream_encoder_destroy(PointerByReference st) {
        codec.opus_multistream_encoder_destroy(st);
    }

    public int opus_multistream_encoder_ctl(PointerByReference st, int request, Object... varargs) {
        return codec.opus_multistream_encoder_ctl(st, request, varargs);
    }

    public int opus_multistream_decoder_get_size(int streams, int coupled_streams) {
        return codec.opus_multistream_decoder_get_size(streams, coupled_streams);
    }

    public PointerByReference opus_multistream_decoder_create(int Fs, int channels, int streams, int coupled_streams, byte[] mapping, IntBuffer error) {
        return codec.opus_multistream_decoder_create(Fs, channels, streams, coupled_streams, mapping, error);
    }

    public int opus_multistream_decoder_init(PointerByReference st, int Fs, int channels, int streams, int coupled_streams, byte[] mapping) {
        return codec.opus_multistream_decoder_init(st, Fs, channels, streams, coupled_streams, mapping);
    }

    public int opus_multistream_decoder_init(PointerByReference st, int Fs, int channels, int streams, int coupled_streams, Pointer mapping) {
        return codec.opus_multistream_decoder_init(st, Fs, channels, streams, coupled_streams, mapping);
    }

    public int opus_multistream_decode(PointerByReference st, byte[] data, int len, ShortBuffer pcm, int frame_size, int decode_fec) {
        return codec.opus_multistream_decode(st, data, len, pcm, frame_size, decode_fec);
    }

    public int opus_multistream_decode(PointerByReference st, Pointer data, int len, ShortByReference pcm, int frame_size, int decode_fec) {
        return codec.opus_multistream_decode(st, data, len, pcm, frame_size, decode_fec);
    }

    public int opus_multistream_decode_float(PointerByReference st, byte[] data, int len, FloatBuffer pcm, int frame_size, int decode_fec) {
        return codec.opus_multistream_decode_float(st, data, len, pcm, frame_size, decode_fec);
    }

    public int opus_multistream_decode_float(PointerByReference st, Pointer data, int len, FloatByReference pcm, int frame_size, int decode_fec) {
        return codec.opus_multistream_decode_float(st, data, len, pcm, frame_size, decode_fec);
    }

    public int opus_multistream_decoder_ctl(PointerByReference st, int request, Object... varargs) {
        return codec.opus_multistream_decoder_ctl(st, request, varargs);
    }

    public void opus_multistream_decoder_destroy(PointerByReference st) {
        codec.opus_multistream_decoder_destroy(st);
    }

    public PointerByReference opus_custom_mode_create(int Fs, int frame_size, IntBuffer error) {
        return codec.opus_custom_mode_create(Fs, frame_size, error);
    }

    public void opus_custom_mode_destroy(PointerByReference mode) {
        codec.opus_custom_mode_destroy(mode);
    }

    public int opus_custom_encoder_get_size(PointerByReference mode, int channels) {
        return codec.opus_custom_encoder_get_size(mode, channels);
    }

    public PointerByReference opus_custom_encoder_create(PointerByReference mode, int channels, IntBuffer error) {
        return codec.opus_custom_encoder_create(mode, channels, error);
    }

    public PointerByReference opus_custom_encoder_create(PointerByReference mode, int channels, IntByReference error) {
        return codec.opus_custom_encoder_create(mode, channels, error);
    }

    public void opus_custom_encoder_destroy(PointerByReference st) {
        codec.opus_custom_encoder_destroy(st);
    }

    public int opus_custom_encode_float(PointerByReference st, float[] pcm, int frame_size, ByteBuffer compressed, int maxCompressedBytes) {
        return codec.opus_custom_encode_float(st, pcm, frame_size, compressed, maxCompressedBytes);
    }

    public int opus_custom_encode_float(PointerByReference st, FloatByReference pcm, int frame_size, Pointer compressed, int maxCompressedBytes) {
        return codec.opus_custom_encode_float(st, pcm, frame_size, compressed, maxCompressedBytes);
    }

    public int opus_custom_encode(PointerByReference st, ShortBuffer pcm, int frame_size, ByteBuffer compressed, int maxCompressedBytes) {
        return codec.opus_custom_encode(st, pcm, frame_size, compressed, maxCompressedBytes);
    }

    public int opus_custom_encode(PointerByReference st, ShortByReference pcm, int frame_size, Pointer compressed, int maxCompressedBytes) {
        return codec.opus_custom_encode(st, pcm, frame_size, compressed, maxCompressedBytes);
    }

    public int opus_custom_encoder_ctl(PointerByReference st, int request, Object... varargs) {
        return codec.opus_custom_encoder_ctl(st, request, varargs);
    }

    public int opus_custom_decoder_get_size(PointerByReference mode, int channels) {
        return codec.opus_custom_decoder_get_size(mode, channels);
    }

    public int opus_custom_decoder_init(PointerByReference st, PointerByReference mode, int channels) {
        return codec.opus_custom_decoder_init(st, mode, channels);
    }

    public PointerByReference opus_custom_decoder_create(PointerByReference mode, int channels, IntBuffer error) {
        return codec.opus_custom_decoder_create(mode, channels, error);
    }

    public PointerByReference opus_custom_decoder_create(PointerByReference mode, int channels, IntByReference error) {
        return codec.opus_custom_decoder_create(mode, channels, error);
    }

    public void opus_custom_decoder_destroy(PointerByReference st) {
        codec.opus_custom_decoder_destroy(st);
    }

    public int opus_custom_decode_float(PointerByReference st, byte[] data, int len, FloatBuffer pcm, int frame_size) {
        return codec.opus_custom_decode_float(st, data, len, pcm, frame_size);
    }

    public int opus_custom_decode_float(PointerByReference st, Pointer data, int len, FloatByReference pcm, int frame_size) {
        return codec.opus_custom_decode_float(st, data, len, pcm, frame_size);
    }

    public int opus_custom_decode(PointerByReference st, byte[] data, int len, ShortBuffer pcm, int frame_size) {
        return codec.opus_custom_decode(st, data, len, pcm, frame_size);
    }

    public int opus_custom_decode(PointerByReference st, Pointer data, int len, ShortByReference pcm, int frame_size) {
        return codec.opus_custom_decode(st, data, len, pcm, frame_size);
    }

    public int opus_custom_decoder_ctl(PointerByReference st, int request, Object... varargs) {
        return codec.opus_custom_decoder_ctl(st, request, varargs);
    }

    public static void assertOpusError(int code) {
        if (code >= 0) return;
        switch (code) {
            case Opus.OPUS_BAD_ARG:
                throw new AudioException("bad argument");
            case Opus.OPUS_BUFFER_TOO_SMALL:
                throw new AudioException("buffer too small");
            case Opus.OPUS_INTERNAL_ERROR:
                throw new AudioException("internal error");
            case Opus.OPUS_INVALID_PACKET:
                throw new AudioException("invalid packet");
            case Opus.OPUS_UNIMPLEMENTED:
                throw new AudioException("unimplemented");
            case Opus.OPUS_INVALID_STATE:
                throw new AudioException("invalid state");
            case Opus.OPUS_ALLOC_FAIL:
                throw new AudioException("memory alloc failed");
        }
    }
}
