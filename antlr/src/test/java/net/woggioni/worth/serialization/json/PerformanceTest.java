package net.woggioni.worth.serialization.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.woggioni.worth.antlr.JSONLexer;
import net.woggioni.worth.antlr.JSONListenerImpl;
import net.woggioni.worth.serialization.binary.JBONDumper;
import net.woggioni.worth.xface.Dumper;
import net.woggioni.worth.xface.Parser;
import net.woggioni.worth.xface.Value;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Ignore;
import org.junit.Test;
import org.tukaani.xz.XZInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class PerformanceTest {

    @SneakyThrows
    private static InputStream extractTestData() {
        return new XZInputStream(new BufferedInputStream(PerformanceTest.class.getResourceAsStream("/citylots.json.xz")));
    }

    private static InputStream smallTestData() {
        return new BufferedInputStream(PerformanceTest.class.getResourceAsStream("/wordpress.json"));
    }

    @Test
    @Ignore
    @SneakyThrows
    public void profilerTest() {
        while (true) {
            Value value = new JSONParser().parse(getClass().getResourceAsStream("/wordpress.json"));
        }
    }

    @Test
    @SneakyThrows
    public void loopTest() {
        double jacksonTime, worthTime, antlrTime;
        final int loops = 100;
        Chronometer chr = new Chronometer();
        {
            ObjectMapper om = new ObjectMapper();
            for(int j = 0; j < 2; j++) {
                chr.reset();
                for (int i = 0; i < loops; i++) {
                    JsonNode jsonNode = om.readTree(smallTestData());
                }
            }
            jacksonTime = chr.stop(Chronometer.TimeUnit.MILLISECOND);
            System.out.printf("Jackson time: %8s msec\n", String.format("%.3f", jacksonTime));
        }
        {
            for(int j = 0; j < 2; j++) {
                chr.reset();
                for(int i = 0; i < loops; i++) {
                    Value value = new JSONParser().parse(smallTestData());
                }
            }
            worthTime = chr.stop(Chronometer.TimeUnit.MILLISECOND);
            System.out.printf("Worth time:   %8s msec\n", String.format("%.3f", worthTime));
        }
        {
            for(int j = 0; j < 2; j++) {
                chr.reset();
                for(int i = 0; i < loops; i++) {
                    CharStream inputStream = CharStreams.fromReader(new InputStreamReader(smallTestData()));
                    JSONLexer lexer = new JSONLexer(inputStream);
                    CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
                    net.woggioni.worth.antlr.JSONParser parser = new net.woggioni.worth.antlr.JSONParser(commonTokenStream);
                    JSONListenerImpl listener = new JSONListenerImpl();
                    ParseTreeWalker walker = new ParseTreeWalker();
                    walker.walk(listener, parser.json());
                }
            }
            antlrTime = chr.stop(Chronometer.TimeUnit.MILLISECOND);
            System.out.printf("Antlr time:   %8s msec\n", String.format("%.3f", antlrTime));
        }
    }

    @Test
    @Ignore
    @SneakyThrows
    public void hugeJSONTest() {
        double jacksonTime, worthTime, antlrTime;
        Chronometer chr = new Chronometer();
        try(InputStream is = extractTestData()) {
            chr.reset();
            ObjectMapper om = new ObjectMapper();
            om.readTree(is);
            jacksonTime = chr.stop(Chronometer.TimeUnit.SECOND);
            System.out.printf("Jackson time: %8s sec\n", String.format("%.3f", jacksonTime));
        }
        try(InputStream is = extractTestData()) {
            chr.reset();
            new JSONParser().parse(is);
            worthTime = chr.stop(Chronometer.TimeUnit.SECOND);
            System.out.printf("Worth time:   %8s sec\n", String.format("%.3f", worthTime));
        }
        try(InputStream is = extractTestData()) {
            chr.reset();
            CharStream inputStream = CharStreams.fromReader(new InputStreamReader(is));
            JSONLexer lexer = new JSONLexer(inputStream);
            CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
            net.woggioni.worth.antlr.JSONParser parser = new net.woggioni.worth.antlr.JSONParser(commonTokenStream);
            JSONListenerImpl listener = new JSONListenerImpl();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, parser.json());
            antlrTime = chr.stop(Chronometer.TimeUnit.SECOND);
            System.out.printf("Antlr time:   %8s sec\n", String.format("%.3f", antlrTime));
        }
    }

    @Test
    @Ignore
    @SneakyThrows
    public void tess() {
        Value value;
        try(InputStream is = extractTestData()) {
            Parser parser = JSONParser.newInstance();
            value = parser.parse(is);
        }
        try(OutputStream os = new BufferedOutputStream(new FileOutputStream("/tmp/citylots.json"))) {
            Dumper dumper = JSONDumper.newInstance();
            dumper.dump(value, os);
        }
        try(OutputStream os = new BufferedOutputStream(new FileOutputStream("/tmp/citylots.jbon"))) {
            Dumper dumper = JBONDumper.newInstance();
            dumper.dump(value, os);
        }
    }
}
