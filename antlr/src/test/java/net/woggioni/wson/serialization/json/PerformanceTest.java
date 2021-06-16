package net.woggioni.wson.serialization.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.woggioni.jwo.Chronometer;
import net.woggioni.wson.antlr.JSONLexer;
import net.woggioni.wson.antlr.JSONListenerImpl;
import net.woggioni.wson.serialization.binary.JBONDumper;
import net.woggioni.wson.serialization.binary.JBONParser;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Dumper;
import net.woggioni.wson.xface.Parser;
import net.woggioni.wson.xface.Value;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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

    @SneakyThrows
    private static InputStream extractBinaryTestData() {
        return new XZInputStream(new BufferedInputStream(PerformanceTest.class.getResourceAsStream("/citylots.jbon.xz")));
    }

    private static InputStream smallTestData() {
        return new BufferedInputStream(PerformanceTest.class.getResourceAsStream("/wordpress.json"));
    }

    @Test
    @Disabled
    @SneakyThrows
    public void profilerTest() {
        while (true) {
            Value value = new JSONParser().parse(getClass().getResourceAsStream("/wordpress.json"));
        }
    }

    @Test
    @SneakyThrows
    public void loopTest() {
        double jacksonTime, wsonTime, antlrTime;
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
            jacksonTime = chr.elapsed(Chronometer.UnitOfMeasure.MILLISECONDS);
            System.out.printf("Jackson time: %8s msec\n", String.format("%.3f", jacksonTime));
        }
        {
            for(int j = 0; j < 2; j++) {
                chr.reset();
                for(int i = 0; i < loops; i++) {
                    Value value = new JSONParser().parse(smallTestData());
                }
            }
            wsonTime = chr.elapsed(Chronometer.UnitOfMeasure.MILLISECONDS);
            System.out.printf("Worth time:   %8s msec\n", String.format("%.3f", wsonTime));
        }
        {
            for(int j = 0; j < 2; j++) {
                chr.reset();
                for(int i = 0; i < loops; i++) {
                    CharStream inputStream = CharStreams.fromReader(new InputStreamReader(smallTestData()));
                    JSONLexer lexer = new JSONLexer(inputStream);
                    CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
                    net.woggioni.wson.antlr.JSONParser parser = new net.woggioni.wson.antlr.JSONParser(commonTokenStream);
                    JSONListenerImpl listener = new JSONListenerImpl();
                    ParseTreeWalker walker = new ParseTreeWalker();
                    walker.walk(listener, parser.json());
                }
            }
            antlrTime = chr.elapsed(Chronometer.UnitOfMeasure.MILLISECONDS);
            System.out.printf("Antlr time:   %8s msec\n", String.format("%.3f", antlrTime));
        }
    }

    @Test
    @Disabled
    @SneakyThrows
    public void hugeJSONTest() {
        double jacksonTime, worthTime, antlrTime;
        Chronometer chr = new Chronometer();
        Value.Configuration cfg = Value.Configuration.builder()
                .objectValueImplementation(ObjectValue.Implementation.ArrayList)
                .build();
        try(InputStream is = extractTestData()) {
            chr.reset();
            ObjectMapper om = new ObjectMapper();
            om.readTree(is);
            jacksonTime = chr.elapsed(Chronometer.UnitOfMeasure.SECONDS);
            System.out.printf("Jackson time: %8s sec\n", String.format("%.3f", jacksonTime));
        }
        try(InputStream is = extractTestData()) {
            chr.reset();
            new JSONParser(cfg).parse(is);
            worthTime = chr.elapsed(Chronometer.UnitOfMeasure.SECONDS);
            System.out.printf("Worth time:   %8s sec\n", String.format("%.3f", worthTime));
        }

        try(InputStream is = extractBinaryTestData()) {
            chr.reset();
            new JBONParser(cfg).parse(is);
            worthTime = chr.elapsed(Chronometer.UnitOfMeasure.SECONDS);
            System.out.printf("Worth time binary:   %8s sec\n", String.format("%.3f", worthTime));
        }

        try(InputStream is = extractTestData()) {
            chr.reset();
            CharStream inputStream = CharStreams.fromReader(new InputStreamReader(is));
            JSONLexer lexer = new JSONLexer(inputStream);
            CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
            net.woggioni.wson.antlr.JSONParser parser = new net.woggioni.wson.antlr.JSONParser(commonTokenStream);
            JSONListenerImpl listener = new JSONListenerImpl();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, parser.json());
            antlrTime = chr.elapsed(Chronometer.UnitOfMeasure.SECONDS);
            System.out.printf("Antlr time:   %8s sec\n", String.format("%.3f", antlrTime));
        }
    }

    @Test
    @Disabled
    @SneakyThrows
    public void test() {
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
