package net.woggioni.worth.benchmark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.woggioni.worth.antlr.JSONLexer;
import net.woggioni.worth.antlr.JSONListenerImpl;
import net.woggioni.worth.serialization.binary.JBONParser;
import net.woggioni.worth.serialization.json.JSONParser;
import net.woggioni.worth.utils.Chronometer;
import net.woggioni.worth.value.ObjectValue;
import net.woggioni.worth.xface.Value;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.tukaani.xz.XZInputStream;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

    @SneakyThrows
    private static InputStream extractTestData() {
        return new XZInputStream(Main.class.getResourceAsStream("/citylots.json.xz"));
    }

    @SneakyThrows
    private static InputStream extractBinaryTestData() {
        return new XZInputStream(new BufferedInputStream(Main.class.getResourceAsStream("/citylots.jbon.xz")));
    }

    private static InputStream smallTestData() {
        return new BufferedInputStream(Main.class.getResourceAsStream("/wordpress.json"));
    }

    @SneakyThrows
    private static void loopBenchmark() {
        double jacksonTime, worthTime, antlrTime;
        final int loops = 100;
        Chronometer chr = new Chronometer();
        {
            ObjectMapper om = new ObjectMapper();
            for (int j = 0; j < 2; j++) {
                chr.reset();
                for (int i = 0; i < loops; i++) {
                    JsonNode jsonNode = om.readTree(smallTestData());
                }
            }
            jacksonTime = chr.stop(Chronometer.TimeUnit.MILLISECOND);
            System.out.printf("Jackson time: %8s msec\n", String.format("%.3f", jacksonTime));
        }
        {
            for (int j = 0; j < 2; j++) {
                chr.reset();
                for (int i = 0; i < loops; i++) {
                    Value value = new JSONParser().parse(new BufferedReader(new InputStreamReader(smallTestData())));
                }
            }
            worthTime = chr.stop(Chronometer.TimeUnit.MILLISECOND);
            System.out.printf("Worth time:   %8s msec\n", String.format("%.3f", worthTime));
        }
        {
            for (int j = 0; j < 2; j++) {
                chr.reset();
                for (int i = 0; i < loops; i++) {
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

    private static Value.Configuration buildConfiguration() {
        return Value.Configuration.builder()
            .objectValueImplementation(ObjectValue.Implementation.ArrayList)
            .build();
    }

    @SneakyThrows
    public static void jacksonBenchmark() {
        Chronometer chr = new Chronometer();
        try (InputStream is = extractTestData()) {
            chr.reset();
            ObjectMapper om = new ObjectMapper();
            om.readTree(new InputStreamReader(is));
            double elapsedTime = chr.stop(Chronometer.TimeUnit.SECOND);
            System.out.printf("Jackson time: %8s sec\n", String.format("%.3f", elapsedTime));
        }
    }

    @SneakyThrows
    public static void worthJsonBenchmark() {
        Chronometer chr = new Chronometer();
        try (InputStream is = extractTestData()) {
            chr.reset();
            new JSONParser(buildConfiguration()).parse(is);
            double elapsedTime = chr.stop(Chronometer.TimeUnit.SECOND);
            System.out.printf("Worth json time:   %8s sec\n", String.format("%.3f", elapsedTime));
        }
    }

    @SneakyThrows
    public static void worthJbonBenchmark() {
        Chronometer chr = new Chronometer();
        try (InputStream is = extractBinaryTestData()) {
            chr.reset();
            new JBONParser(buildConfiguration()).parse(is);
            double elapsedTime = chr.stop(Chronometer.TimeUnit.SECOND);
            System.out.printf("Worth jbon time:   %8s sec\n", String.format("%.3f", elapsedTime));
        }
    }

    @SneakyThrows
    public static void antlrBenchmark() {
        Chronometer chr = new Chronometer();
        try (InputStream is = extractTestData()) {
            chr.reset();
            CharStream inputStream = CharStreams.fromReader(new InputStreamReader(is));
            JSONLexer lexer = new JSONLexer(inputStream);
            CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
            net.woggioni.worth.antlr.JSONParser parser = new net.woggioni.worth.antlr.JSONParser(commonTokenStream);
            JSONListenerImpl listener = new JSONListenerImpl();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, parser.json());
            double elapsedTime = chr.stop(Chronometer.TimeUnit.SECOND);
            System.out.printf("Antlr time:   %8s sec\n", String.format("%.3f", elapsedTime));
        }
    }

    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("Benchmark names expected as command line arguments");
            System.exit(-1);
        }
        Method[] methods = Main.class.getMethods();
        for(String benchmarkName : args) {
            Optional<Method> targetMethod = Arrays.stream(methods)
                    .filter(method -> Objects.equals(benchmarkName, method.getName()))
                    .findFirst();
            targetMethod.ifPresent(new Consumer<>() {
                @Override
                @SneakyThrows
                public void accept(Method method) {
                    method.invoke(null);
                }
            });
            targetMethod.orElseThrow(() -> {
                List<String> benchmarkNames = Arrays.stream(methods)
                        .filter(m -> m.getName()
                                .endsWith("Benchmark"))
                        .map(Method::getName)
                        .collect(Collectors.toList());
                String msg = "Unknown benchmark '" + benchmarkName +
                        "', available benchmarks are: " +
                        String.join(", ", benchmarkNames);
                return new IllegalArgumentException(msg);
            });
        }
    }
}
