package xyz.cursedman.filecrypto.cryptors.aead.chacha20;

import org.junit.jupiter.api.Test;
import xyz.cursedman.filecrypto.cryptors.Cryptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChaCha20CryptorTest {

    private ChaCha20CryptorKey generateRandomKey() {
        byte[] keyBytes = new byte[ChaCha20CryptorKey.KEY_SIZE_BYTES];
        new SecureRandom().nextBytes(keyBytes);
        return ChaCha20CryptorKey.fromBytes(keyBytes);
    }

    private byte[] encrypt(Cryptor cryptor, byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cryptor.encrypt(new ByteArrayInputStream(data), out);
        return out.toByteArray();
    }

    private byte[] decrypt(Cryptor cryptor, byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cryptor.decrypt(new ByteArrayInputStream(data), out);
        return out.toByteArray();
    }

    @Test
    void encryptAndDecrypt() throws Exception {
        byte[] original = new byte[10_000];
        new Random(42).nextBytes(original);

        Cryptor cryptor = new ChaCha20Cryptor(generateRandomKey());

        assertArrayEquals(original, decrypt(cryptor, encrypt(cryptor, original)));
    }

    @Test
    void sameInputEncryptsDifferently() throws Exception {
        byte[] original = {1, 2, 3, 4};
        Cryptor cryptor = new ChaCha20Cryptor(generateRandomKey());

        assertFalse(Arrays.equals(encrypt(cryptor, original), encrypt(cryptor, original)));
    }

    @Test
    void decryptWithWrongKeyFails() throws Exception {
        byte[] encrypted = encrypt(new ChaCha20Cryptor(generateRandomKey()), new byte[]{1, 2, 3});

        assertThrows(IOException.class, () -> decrypt(new ChaCha20Cryptor(generateRandomKey()), encrypted));
    }

    @Test
    void decryptTamperedDataFails() throws Exception {
        Cryptor cryptor = new ChaCha20Cryptor(generateRandomKey());
        byte[] encrypted = encrypt(cryptor, new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        encrypted[encrypted.length / 2] ^= 1;

        assertThrows(IOException.class, () -> decrypt(cryptor, encrypted));
    }

    @Test
    void keyWithInvalidLengthIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ChaCha20CryptorKey.fromBytes(new byte[16]));
    }
}
