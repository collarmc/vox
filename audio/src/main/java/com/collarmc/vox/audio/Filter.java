package com.collarmc.vox.audio;

/**
 * Filters an audio frame buffer
 */
@FunctionalInterface
public interface Filter {
    /**
     * Filters an audio frame buffer
     * @param frameBuffer to filter
     * @return filtered frame buffer
     */
    byte[] filter(byte[] frameBuffer);
}
