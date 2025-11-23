package xyz.cursedman.filecrypto.cryptors.AESCryptor;

import org.junit.jupiter.api.Test;
import xyz.cursedman.filecrypto.cryptors.Cryptor;

import javax.crypto.KeyGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class AESCryptorTest {

    private AESCryptorKey generateRandomKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // 128, 192, or 256 bits depending on JCE policy
            return new AESCryptorKey(keyGen.generateKey());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES key generation failed", e);
        }
    }

    @Test
    void encryptAndDecrypt() throws Exception {
        byte[] original = {0, 1, 2, 3, 4, 5, (byte) 250, (byte) 255};

        AESCryptorKey key = generateRandomKey();
        Cryptor aesCryptor = new AESCryptor(key);

        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        aesCryptor.encrypt(new ByteArrayInputStream(original), encryptedOut);
        byte[] encrypted = encryptedOut.toByteArray();

        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
        aesCryptor.decrypt(new ByteArrayInputStream(encrypted), decryptedOut);
        byte[] decrypted = decryptedOut.toByteArray();

        assertArrayEquals(original, decrypted);
    }
}
