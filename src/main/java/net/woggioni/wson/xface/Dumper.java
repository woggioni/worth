package net.woggioni.wson.xface;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

public interface Dumper {
    void dump(Value value, OutputStream is);

    void dump(Value value, Writer writer);

    void dump(Value value, OutputStream stream, Charset encoding);
}
