package org.oggio88.worth.xface;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

public interface Dumper {
    void dump(Value value, OutputStream is);

    void dump(Value value, Writer reader);

    void dump(Value value, OutputStream stream, Charset encoding);
}
