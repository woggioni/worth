package net.woggioni.worth.buffer;

import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Random;

public class CircularBufferTest {

    @Test
    @SneakyThrows
    public void test() {
        MessageDigest streamDigest = MessageDigest.getInstance("MD5"), outputDigest = MessageDigest.getInstance("MD5");
        InputStream is = new DigestInputStream(getClass().getResourceAsStream("/logging.properties"), streamDigest);
        CircularBuffer cb = new CircularBuffer(new InputStreamReader(is), 32);
        Random rand = new Random();
        while (true) {
            int b = cb.next();
            if (b < 0) break;
            if (rand.nextInt() % 2 == 0) {
                cb.prev();
            } else {
                char c = (char) b;
                outputDigest.update((byte) b);
                System.out.print(c);
            }
        }
        System.out.println();
        Assert.assertArrayEquals(streamDigest.digest(), outputDigest.digest());
    }
}
