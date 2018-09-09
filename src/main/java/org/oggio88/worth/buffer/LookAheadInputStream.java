package org.oggio88.worth.buffer;

import java.io.IOException;
import java.io.InputStream;

public class LookAheadInputStream extends InputStream {

    private final InputStream stream;
    private int currentByte;

    LookAheadInputStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        int result = currentByte;
        currentByte = stream.read();
        return result;
    }

    public int getCurrentByte(){
        return currentByte;
    }
}
