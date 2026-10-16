package xyz.cursedman.filecrypto.cryptors.aead.aesgcm;

import org.junit.jupiter.api.Test;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.aes.AESCryptorKey;

import javax.crypto.KeyGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AESGCMCryptorTest {

    private AESCryptorKey generateRandomKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return new AESCryptorKey(keyGen.generateKey());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES key generation failed", e);
        }
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

        Cryptor cryptor = new AESGCMCryptor(generateRandomKey());

        assertArrayEquals(original, decrypt(cryptor, encrypt(cryptor, original)));
    }

    @Test
    void decryptKnownVector() throws Exception {
        HexFormat hex = HexFormat.of();
        AESCryptorKey key = AESCryptorKey.fromBytes(hex.parseHex("feffe9928665731c6d6a8f9467308308"));
        byte[] encrypted = hex.parseHex(
            "cafebabefacedbaddecaf888"
                + "42831ec2217774244b7221b784d0d49ce3aa212f2c02a4e035c17e2329aca12e"
                + "21d514b25466931c7d8f6a5aac84aa051ba30b396a0aac973d58e091473f5985"
                + "4d5c2af327cd64a62cf35abd2ba6fab4"
        );
        byte[] expected = hex.parseHex(
            "d9313225f88406e5a55909c5aff5269a86a7a9531534f7da2e4c303d8a318a72"
                + "1c3c0c95956809532fcf0e2449a6b525b16aedf5aa0de657ba637b391aafd255"
        );

        assertArrayEquals(expected, decrypt(new AESGCMCryptor(key), encrypted));
    }

    @Test
    void sameInputEncryptsDifferently() throws Exception {
        byte[] original = {1, 2, 3, 4};
        Cryptor cryptor = new AESGCMCryptor(generateRandomKey());

        assertFalse(Arrays.equals(encrypt(cryptor, original), encrypt(cryptor, original)));
    }

    @Test
    void decryptWithWrongKeyFails() throws Exception {
        byte[] encrypted = encrypt(new AESGCMCryptor(generateRandomKey()), new byte[]{1, 2, 3});

        assertThrows(IOException.class, () -> decrypt(new AESGCMCryptor(generateRandomKey()), encrypted));
    }

    @Test
    void decryptTamperedDataFails() throws Exception {
        Cryptor cryptor = new AESGCMCryptor(generateRandomKey());
        byte[] encrypted = encrypt(cryptor, new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        encrypted[encrypted.length / 2] ^= 1;

        assertThrows(IOException.class, () -> decrypt(cryptor, encrypted));
    }
}
