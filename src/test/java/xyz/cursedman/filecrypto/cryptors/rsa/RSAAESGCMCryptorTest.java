package xyz.cursedman.filecrypto.cryptors.rsa;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.cursedman.filecrypto.cryptors.rsa.RSATestSupport.*;

class RSAAESGCMCryptorTest {

    private static final int OVERHEAD = 256 + 12 + 16; // wrapped key + nonce + tag

    private static RSACryptorKey pair;

    @BeforeAll
    static void setUp() {
        pair = generateKeyPair();
    }

    private static byte[] randomBytes(int size) {
        byte[] data = new byte[size];
        new Random(42).nextBytes(data);
        return data;
    }

    @Test
    void encryptAndDecrypt() throws Exception {
        byte[] original = randomBytes(100_000);
        byte[] encrypted = encrypt(new RSAAESGCMCryptor(publicOnly(pair)), original);

        assertEquals(original.length + OVERHEAD, encrypted.length);
        assertArrayEquals(original, decrypt(new RSAAESGCMCryptor(privateOnly(pair)), encrypted));
    }

    @Test
    void emptyInput() throws Exception {
        byte[] encrypted = encrypt(new RSAAESGCMCryptor(pair), new byte[0]);

        assertEquals(OVERHEAD, encrypted.length);
        assertArrayEquals(new byte[0], decrypt(new RSAAESGCMCryptor(pair), encrypted));
    }

    @Test
    void tamperedWrappedKeyFails() throws Exception {
        byte[] encrypted = encrypt(new RSAAESGCMCryptor(pair), randomBytes(1000));
        encrypted[10] ^= 1;

        assertThrows(IOException.class, () -> decrypt(new RSAAESGCMCryptor(pair), encrypted));
    }

    @Test
    void tamperedDataFails() throws Exception {
        byte[] encrypted = encrypt(new RSAAESGCMCryptor(pair), randomBytes(1000));
        encrypted[encrypted.length - 100] ^= 1;

        assertThrows(IOException.class, () -> decrypt(new RSAAESGCMCryptor(pair), encrypted));
    }

    @Test
    void wrongPrivateKeyFails() throws Exception {
        byte[] encrypted = encrypt(new RSAAESGCMCryptor(pair), randomBytes(1000));
        RSACryptorKey other = privateOnly(generateKeyPair());

        assertThrows(IOException.class, () -> decrypt(new RSAAESGCMCryptor(other), encrypted));
    }
}
