package xyz.cursedman.filecrypto.cryptors.XorCryptor;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
}
