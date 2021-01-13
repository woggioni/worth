package net.woggioni.wson.xface;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

public interface Parser {

    Value parse(InputStream is);

    Value parse(Reader reader);

    Value parse(InputStream stream, Charset encoding);
}
