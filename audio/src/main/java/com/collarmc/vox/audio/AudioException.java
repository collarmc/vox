package com.collarmc.vox.audio;

public class AudioException extends RuntimeException {
    public AudioException(String message) {
        super(message);
    }

    public AudioException(String message, Throwable cause) {
        super(message, cause);
    }
}
