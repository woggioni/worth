package net.woggioni.worth.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.OutputStream;

public class Leb128 {

    public static long reverse(long n) {
        long res = 0;
        for(int i = 0; i < 8; i++) {
            long b = (n & (0xFFL << (i * 8))) >>> (i * 8);
            res |= b << ((7 - i) * 8);
        }
        return res;
    }

    public static int encode(OutputStream os, double input) {
        return encode(os, reverse(Double.doubleToLongBits(input)));
    }

    @SneakyThrows
    public static int encode(OutputStream os, long input) {
        int bytes_written = 0;
        long number = input >= 0 ? (input << 1) : (-(input + 1)) << 1 | 1;
        while(number > 127) {
            os.write((int) (number & 127) | 128);
            bytes_written++;
            number >>= 7;
        }
        os.write((int) number);
        return ++bytes_written;
    }

    @RequiredArgsConstructor
    public static class Leb128Decoder {
        @Getter
        private int bytesRead = 0;

        private final InputStream is;

        public byte decodeByte() {
            return (byte) decode();
        }

        public short decodeShort() {
            return (short) decode();
        }

        public int decodeInt() {
            return (int) decode();
        }

        public double decodeDouble() {
            return Double.longBitsToDouble(reverse(decode()));
        }

        @SneakyThrows
        public long decode() {
            long res = 0;
            for(int i = 0; i < (8 * 8 + 6) / 7; i++) {
                int c = is.read();
                bytesRead++;
                if(c < 0) {
                    throw new IllegalArgumentException("Unexpected end of file");
                }
                byte b = (byte) c;
                res |= ((long)(b & 127)) << (i * 7);
                if(b >= 0) break;
            }
            return (res & 1) != 0 ? - (res >> 1) - 1 : (res >> 1);
        }
    }
}
