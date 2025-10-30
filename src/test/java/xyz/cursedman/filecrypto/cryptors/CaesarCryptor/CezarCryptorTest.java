package xyz.cursedman.filecrypto.cryptors.CaesarCryptor;

import org.junit.jupiter.api.Test;
import xyz.cursedman.filecrypto.cryptors.Cryptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CezarCryptorTest {
    Cryptor cezarCryptor = CaesarCryptor.builder().key(
            CaesarCryptorKey.builder()
                    .shift(3)
                    .build()
    ).build();

    byte[] original = {0, 1, 2, 3, 4, 5, (byte) 250, (byte) 255};
    // Expected result after shift = 3
    byte[] expectedEncrypted = {3, 4, 5, 6, 7, 8, (byte) 253, 2};

    @Test
    void encrypt() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cezarCryptor.encrypt(new ByteArrayInputStream(original), out);
        assertArrayEquals(expectedEncrypted, out.toByteArray());
    }

    @Test
    void decrypt() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cezarCryptor.decrypt(new ByteArrayInputStream(expectedEncrypted), out);
        assertArrayEquals(original, out.toByteArray());
    }

}