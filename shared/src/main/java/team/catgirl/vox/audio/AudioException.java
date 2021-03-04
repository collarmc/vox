package team.catgirl.vox.audio;

import static tomp2p.opuswrapper.Opus.*;

public class AudioException extends RuntimeException {
    public AudioException(String message) {
        super(message);
    }

    public AudioException(String message, Throwable cause) {
        super(message, cause);
    }

    static void assertOpusError(int code) {
        if (code >= 0) return;
        switch (code) {
            case OPUS_BAD_ARG:
                throw new AudioException("bad argument");
            case OPUS_BUFFER_TOO_SMALL:
                throw new AudioException("buffer too small");
            case OPUS_INTERNAL_ERROR:
                throw new AudioException("internal error");
            case OPUS_INVALID_PACKET:
                throw new AudioException("invalid packet");
            case OPUS_UNIMPLEMENTED:
                throw new AudioException("unimplemented");
            case OPUS_INVALID_STATE:
                throw new AudioException("invalid state");
            case OPUS_ALLOC_FAIL:
                throw new AudioException("memory alloc failed");
        }
    }
}
