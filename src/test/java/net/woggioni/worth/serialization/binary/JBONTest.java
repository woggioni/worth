package net.woggioni.worth.serialization.binary;

import lombok.SneakyThrows;
import net.woggioni.worth.buffer.LookAheadTextInputStream;
import net.woggioni.worth.serialization.json.JSONParser;
import net.woggioni.worth.value.ObjectValue;
import net.woggioni.worth.xface.Parser;
import net.woggioni.worth.xface.Value;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JBONTest {

    private String[] testFiles = new String[]{"/test.json", "/wordpress.json"};

    private InputStream getTestSource(String filename) {
        return getClass().getResourceAsStream(filename);
    }

    @Test
    @SneakyThrows
    public void consistencyTest() {
        System.setProperty(ObjectValue.class.getName() + ".implementation", "TreeMap");
        for (String testFile : testFiles) {
            Value parsedValue;
            try(InputStream is = getTestSource(testFile)) {
                Parser parser = new JSONParser();
                parsedValue = parser.parse(is);
            }
            byte[] dumpedJBON;
            try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                JBONDumper.newInstance().dump(parsedValue, baos);
                dumpedJBON = baos.toByteArray();
            }
            Value reParsedValue;
            try(InputStream is = new ByteArrayInputStream(dumpedJBON)) {
                Parser parser = new JBONParser();
                reParsedValue = parser.parse(is);
            }
            Assert.assertEquals(parsedValue, reParsedValue);
            byte[] reDumpedJBON;
            try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                JBONDumper.newInstance().dump(reParsedValue, baos);
                reDumpedJBON = baos.toByteArray();
            }
            Assert.assertArrayEquals(dumpedJBON, reDumpedJBON);
        }
    }

    @Test
    @SneakyThrows
    public void comparativeTest() {
        for (String testFile : testFiles) {
            Value originalValue = new JSONParser().parse(getTestSource(testFile));

            Path outputFile = Files.createTempFile(Paths.get("/tmp"),"worth", null);
            try (OutputStream os = new FileOutputStream(outputFile.toFile())) {
                JBONDumper jbonDumper = new JBONDumper();
                jbonDumper.dump(originalValue, os);
            }
            Value binarySerializedValue;
            try(InputStream is = new FileInputStream(outputFile.toFile())) {
                JBONParser jbonParser = new JBONParser();
                binarySerializedValue = jbonParser.parse(is);
            }
            Assert.assertEquals(originalValue, binarySerializedValue);
        }
    }

    @Test
    @SneakyThrows
    public void hexTest() {
        String hex = "1F608";
        byte[] buffer = hex.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        Method method = JSONParser.class.getDeclaredMethod("parseHex", LookAheadTextInputStream.class);
        method.setAccessible(true);
        LookAheadTextInputStream ltis = new LookAheadTextInputStream(new InputStreamReader(bais));
        ltis.read();
        int result = (int) method.invoke(null, ltis);
        Assert.assertEquals((int) Integer.valueOf(hex, 16), result);
    }
}
