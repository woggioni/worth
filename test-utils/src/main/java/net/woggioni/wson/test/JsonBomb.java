package net.woggioni.wson.test;

import java.io.InputStream;

public class JsonBomb {
    public static InputStream infiniteJson() {
        return new InputStream() {
            int index = 0;
            final String monomer = "{\"key\":[";
            @Override
            public int read() {
                return monomer.charAt(index++ % monomer.length());
            }
        };
    }
}
