package com.collarmc.vox.audio.opus;

import club.minnced.opus.util.OpusLibrary;

import java.io.IOException;

public final class OpusSettings {
    public static final int OPUS_SAMPLE_RATE = 48000;
    public static final int OPUS_FRAME_SIZE = 960;
    public static final int OPUS_FRAME_TIME_AMOUNT = 20;
    public static final int OPUS_CHANNEL_COUNT = 2;
    public static final int OPUS_BUFFER_SIZE = OpusSettings.OPUS_FRAME_SIZE * OpusSettings.OPUS_CHANNEL_COUNT * 2;

    public static void initializeCodec() {
        try {
            OpusLibrary.loadFromJar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OpusSettings() {}
}
