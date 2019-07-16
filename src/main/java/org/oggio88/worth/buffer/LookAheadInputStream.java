package org.oggio88.worth.buffer;

import lombok.SneakyThrows;

import java.io.InputStream;

public class LookAheadInputStream extends InputStream {

    private final InputStream stream;
    private int currentByte;

    public LookAheadInputStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    @SneakyThrows
    public int read() {
        int result = currentByte;
        currentByte = stream.read();
        return result;
    }

    public int getCurrentByte() {
        return currentByte;
    }
}
