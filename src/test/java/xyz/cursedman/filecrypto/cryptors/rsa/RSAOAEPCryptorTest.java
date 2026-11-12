package xyz.cursedman.filecrypto.cryptors.rsa;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.cursedman.filecrypto.exceptions.InvalidKeyException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.cursedman.filecrypto.cryptors.rsa.RSATestSupport.*;

class RSAOAEPCryptorTest {

    private static final int MODULUS_BYTES = 256;
    private static final int PLAIN_BLOCK = 190;

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

    private static void assertRoundTrip(byte[] original) throws IOException {
        byte[] encrypted = encrypt(new RSAOAEPCryptor(publicOnly(pair)), original);
        int blocks = (original.length + PLAIN_BLOCK - 1) / PLAIN_BLOCK;

        assertEquals(blocks * MODULUS_BYTES, encrypted.length);
        assertArrayEquals(original, decrypt(new RSAOAEPCryptor(privateOnly(pair)), encrypted));
    }

    @Test
    void encryptAndDecrypt() throws Exception {
        assertRoundTrip(randomBytes(10_000));
    }

    @Test
    void emptyInput() throws Exception {
        assertRoundTrip(new byte[0]);
    }

    @Test
    void inputExactMultipleOfBlock() throws Exception {
        assertRoundTrip(randomBytes(PLAIN_BLOCK * 3));
    }

    @Test
    void tamperedDataFails() throws Exception {
        byte[] encrypted = encrypt(new RSAOAEPCryptor(pair), randomBytes(1000));
        encrypted[300] ^= 1;

        assertThrows(IOException.class, () -> decrypt(new RSAOAEPCryptor(pair), encrypted));
    }

    @Test
    void truncatedDataFails() throws Exception {
        byte[] encrypted = encrypt(new RSAOAEPCryptor(pair), randomBytes(1000));
        byte[] truncated = Arrays.copyOf(encrypted, encrypted.length - 10);

        assertThrows(IOException.class, () -> decrypt(new RSAOAEPCryptor(pair), truncated));
    }

    @Test
    void wrongPrivateKeyFails() throws Exception {
        byte[] encrypted = encrypt(new RSAOAEPCryptor(pair), randomBytes(1000));
        RSACryptorKey other = privateOnly(generateKeyPair());

        assertThrows(IOException.class, () -> decrypt(new RSAOAEPCryptor(other), encrypted));
    }

    @Test
    void decryptWithPublicKeyOnlyFails() throws Exception {
        RSACryptorKey publicKey = publicOnly(pair);
        byte[] encrypted = encrypt(new RSAOAEPCryptor(publicKey), randomBytes(100));

        assertThrows(InvalidKeyException.class, () -> decrypt(new RSAOAEPCryptor(publicKey), encrypted));
    }
}
