package xyz.cursedman.filecrypto.cryptors.rsa;

import xyz.cursedman.filecrypto.cryptors.AsymmetricCryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.cryptors.aead.aesgcm.AESGCMCryptor;
import xyz.cursedman.filecrypto.cryptors.aes.AESCryptorKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/*
 * Hybrid encryption: random AES-256 key wrapped with RSA-OAEP, data encrypted with AES-GCM.
 * Format: [wrapped AES key (modulus size) | AES-GCM output]
 */
public class RSAAESGCMCryptor extends AsymmetricCryptor {

    private static final int AES_KEY_SIZE_BITS = 256;

    public RSAAESGCMCryptor(RSACryptorKey key) {
        super(key);
    }

    @Override
    public CryptorAlgorithm getAlgorithm() {
        return CryptorAlgorithm.RSA_OAEP_AES_GCM;
    }

    @Override
    public void encrypt(InputStream in, OutputStream out) throws IOException {
        AESCryptorKey aesKey;

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE_BITS, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();

            Cipher rsa = RSAOAEP.createCipher(Cipher.ENCRYPT_MODE, getEncryptionKey());
            out.write(rsa.doFinal(secretKey.getEncoded()));
            aesKey = new AESCryptorKey(secretKey);
        } catch (GeneralSecurityException e) {
            throw new IOException("Encryption failed", e);
        }

        new AESGCMCryptor(aesKey).encrypt(in, out);
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws IOException {
        byte[] wrappedKey = new byte[((RSACryptorKey) getKey()).getModulusBytes()];
        new DataInputStream(in).readFully(wrappedKey);

        AESCryptorKey aesKey;

        try {
            Cipher rsa = RSAOAEP.createCipher(Cipher.DECRYPT_MODE, getDecryptionKey());
            aesKey = AESCryptorKey.fromBytes(rsa.doFinal(wrappedKey));
        } catch (GeneralSecurityException e) {
            throw new IOException("Decryption failed: wrong key or corrupted file", e);
        }

        new AESGCMCryptor(aesKey).decrypt(in, out);
    }
}
