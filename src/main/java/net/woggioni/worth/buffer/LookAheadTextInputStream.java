package net.woggioni.worth.buffer;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.Reader;

public class LookAheadTextInputStream extends InputStream {

    private final Reader reader;
    private int currentByte;

    public LookAheadTextInputStream(Reader reader) {
        this.reader = reader;
    }

    @Override
    @SneakyThrows
    public int read() {
        int result = currentByte;
        currentByte = reader.read();
        return result;
    }

    public int getCurrentByte(){
        return currentByte;
    }
}
