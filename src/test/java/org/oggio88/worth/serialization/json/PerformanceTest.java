package org.oggio88.worth.serialization.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Ignore;
import org.junit.Test;
import org.oggio88.worth.antlr.JSONLexer;
import org.oggio88.worth.antlr.JSONListenerImpl;
import org.oggio88.worth.xface.Value;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

class Chronometer {

    public enum TimeUnit {
        NANOSECOND(1e-9), MICROSECOND(1e-6), MILLISECOND(1e-3), SECOND(1);

        private double factor;

        TimeUnit(double factor) {
            this.factor = factor;
        }
    }

    private long start = System.nanoTime();

    public void start() {
        start = System.nanoTime();
    }

    public void reset() {
        start();
    }

    public double stop(TimeUnit unit) {
        return (System.nanoTime() - start) / (1e9 * unit.factor);
    }

    public double stop() {
        return stop(TimeUnit.MILLISECOND);
    }

}

public class PerformanceTest {

    @SneakyThrows
    private static byte[] extractTestData() {
        ByteArrayOutputStream baous = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 1024];
        try (InputStream is = new XZInputStream(PerformanceTest.class.getResourceAsStream("/citylots.json.xz"))) {
            while (true) {
                int read = is.read(buffer);
                if (read < 0) break;
                baous.write(buffer, 0, read);
            }
        }
        return baous.toByteArray();
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
            chr.reset();
            for (int i = 0; i < loops; i++) {
                ObjectMapper om = new ObjectMapper();
                JsonNode jsonNode = om.readTree(getClass().getResourceAsStream("/wordpress.json"));
            }
            jacksonTime = chr.stop(Chronometer.TimeUnit.MILLISECOND);
            System.out.printf("Jackson time: %8s msec\n", String.format("%.3f", jacksonTime));
        }
        {
            chr.reset();
            for (int i = 0; i < loops; i++) {
                Value value = new JSONParser().parse(getClass().getResourceAsStream("/wordpress.json"));
            }
            worthTime = chr.stop(Chronometer.TimeUnit.MILLISECOND);
            System.out.printf("Worth time:   %8s msec\n", String.format("%.3f", worthTime));
        }
        {
            chr.reset();
            for (int i = 0; i < loops; i++) {
                ANTLRInputStream inputStream = new ANTLRInputStream(
                        getClass().getResourceAsStream("/wordpress.json"));
                JSONLexer lexer = new JSONLexer(inputStream);
                CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
                org.oggio88.worth.antlr.JSONParser parser = new org.oggio88.worth.antlr.JSONParser(commonTokenStream);
                JSONListenerImpl listener = new JSONListenerImpl();
                ParseTreeWalker walker = new ParseTreeWalker();
                walker.walk(listener, parser.json());
            }
            antlrTime = chr.stop(Chronometer.TimeUnit.MILLISECOND);
            System.out.printf("Antlr time:   %8s msec\n", String.format("%.3f", antlrTime));
        }
    }

    @Test
    @Ignore
    @SneakyThrows
    public void hugeJSONTest() {
        byte[] testData = extractTestData();
        double jacksonTime, worthTime, antlrTime;
        Chronometer chr = new Chronometer();
        {
            chr.reset();
            ObjectMapper om = new ObjectMapper();
            JsonNode jsonNode = om.readTree(new ByteArrayInputStream(testData));
            jacksonTime = chr.stop(Chronometer.TimeUnit.SECOND);
            System.out.printf("Jackson time: %8s sec\n", String.format("%.3f", jacksonTime));
        }
        {
            chr.reset();
            Value value = new JSONParser().parse(new ByteArrayInputStream(testData));
            worthTime = chr.stop(Chronometer.TimeUnit.SECOND);
            System.out.printf("Worth time:   %8s sec\n", String.format("%.3f", worthTime));
        }
        {
            chr.reset();
            ANTLRInputStream inputStream = new ANTLRInputStream(new ByteArrayInputStream(testData));
            JSONLexer lexer = new JSONLexer(inputStream);
            CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
            org.oggio88.worth.antlr.JSONParser parser = new org.oggio88.worth.antlr.JSONParser(commonTokenStream);
            JSONListenerImpl listener = new JSONListenerImpl();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, parser.json());
            antlrTime = chr.stop(Chronometer.TimeUnit.SECOND);
            System.out.printf("Antlr time:   %8s sec\n", String.format("%.3f", antlrTime));
        }
    }
}
