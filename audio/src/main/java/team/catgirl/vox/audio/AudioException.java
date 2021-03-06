package team.catgirl.vox.audio;

import tomp2p.opuswrapper.Opus;

public class AudioException extends RuntimeException {
    public AudioException(String message) {
        super(message);
    }

    public AudioException(String message, Throwable cause) {
        super(message, cause);
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
