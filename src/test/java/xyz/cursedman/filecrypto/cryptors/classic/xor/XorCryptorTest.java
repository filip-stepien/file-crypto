package xyz.cursedman.filecrypto.cryptors.classic.xor;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class XorCryptorTest {

    byte[] key = {1, 2, 3};
    XorCryptor cryptor = XorCryptor.builder()
            .key(XorCryptorKey.builder().key(key).build())
            .build();

    byte[] original = {
            (byte) 0b00001010, // 10
            (byte) 0b00010100, // 20
            (byte) 0b00011110, // 30
            (byte) 0b00101000, // 40
            (byte) 0b00110010  // 50
    };
    byte[] expectedEncrypted = {
            (byte) 0b00001011, // 11
            (byte) 0b00010110, // 22
            (byte) 0b00011101, // 29
            (byte) 0b00101001, // 41
            (byte) 0b00110000  // 48
    };

    @Test
    void encrypt() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cryptor.encrypt(new ByteArrayInputStream(original), out);
        assertArrayEquals(expectedEncrypted, out.toByteArray());
    }

    @Test
    void decrypt() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cryptor.decrypt(new ByteArrayInputStream(expectedEncrypted), out);
        assertArrayEquals(original, out.toByteArray());
    }

    @Test
    void encryptKeepsKeyPositionAcrossBufferBoundaries() throws Exception {
        byte[] longKey = {7, 13, 42, 99, 1, 5, 77};
        XorCryptor longKeyCryptor = XorCryptor.builder()
                .key(XorCryptorKey.builder().key(longKey).build())
                .build();
        byte[] data = new byte[3 * 8192 + 17];
        new Random(42).nextBytes(data);

        byte[] expected = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            expected[i] = (byte) (data[i] ^ longKey[i % longKey.length]);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        longKeyCryptor.encrypt(new ShortReadInputStream(data), out);
        assertArrayEquals(expected, out.toByteArray());
    }

    private static class ShortReadInputStream extends ByteArrayInputStream {

        ShortReadInputStream(byte[] data) {
            super(data);
        }

        @Override
        public int read(byte[] b, int off, int len) {
            return super.read(b, off, Math.min(len, 1000));
        }
    }
}
