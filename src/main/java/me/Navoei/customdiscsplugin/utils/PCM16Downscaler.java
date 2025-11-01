package me.Navoei.customdiscsplugin.utils;

import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class PCM16Downscaler extends InputStream {
    private final AudioInputStream source;
    private final int sourceBytesPerSample;
    private final int channels;
    private final byte[] sourceBuffer;

    public PCM16Downscaler(AudioInputStream source) {
        this.source = source;
        AudioFormat originalAudioFormat = source.getFormat();
        this.sourceBytesPerSample = originalAudioFormat.getSampleSizeInBits() / 8;
        this.channels = originalAudioFormat.getChannels();
        this.sourceBuffer = new byte[16384 * channels * sourceBytesPerSample];
    }

    @Override
    public int read() throws IOException {
        byte[] oneByte = new byte[1];
        return read(oneByte, 0, 1) == -1 ? -1 : (oneByte[0] & 0xFF);
    }

    @Override
    public int read(byte @NotNull [] readByte, int startOffset, int readLength) throws IOException {
        int maxFramesOut = readLength / (2 * channels);
        int maxInputBytes = maxFramesOut * sourceBytesPerSample * channels;

        int bytesRead = source.read(sourceBuffer, 0, Math.min(sourceBuffer.length, maxInputBytes));
        if (bytesRead <= 0) return -1;

        int sourceIndex = 0;
        int destinationIndex = startOffset;
        int frameSizeIn = sourceBytesPerSample * channels;
        int frames = bytesRead / frameSizeIn;

        for (int frame = 0; frame < frames; frame++) {
            for (int channel = 0; channel < channels; channel++) {
                int sample = 0;
                for (int i = 0; i < sourceBytesPerSample; i++) {
                    sample |= (sourceBuffer[sourceIndex++] & 0xFF) << (8 * i);
                }
                short sampleTo16 = (short) (sample >> ((sourceBytesPerSample - 2) * 8));
                readByte[destinationIndex++] = (byte) sampleTo16;
                readByte[destinationIndex++] = (byte) (sampleTo16 >> 8);
            }
        }

        return destinationIndex - startOffset;
    }
}