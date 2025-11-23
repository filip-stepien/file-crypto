package xyz.cursedman.filecrypto.cryptors.AESCryptor;

import xyz.cursedman.filecrypto.cryptors.Cryptor;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

@SuppressWarnings("unused")
public class AESCryptor extends Cryptor {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16; // AES block size in bytes

    public AESCryptor(AESCryptorKey key) {
        super(key);
    }

    @Override
    public void encrypt(InputStream in, OutputStream out) throws IOException {
        try {
            // Generate random IV
            byte[] iv = new byte[IV_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, ((AESCryptorKey) getKey()).getSecretKey(), ivSpec);

            out.write(iv);

            try (CipherOutputStream cos = new CipherOutputStream(out, cipher)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    cos.write(buffer, 0, read);
                }
            }
        } catch (GeneralSecurityException e) {
            throw new IOException("Encryption failed", e);
        }
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] iv = new byte[IV_SIZE];
            if (in.read(iv) != IV_SIZE) {
                throw new IOException("Unable to read IV from input");
            }
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, ((AESCryptorKey) getKey()).getSecretKey(), ivSpec);

            try (CipherInputStream cis = new CipherInputStream(in, cipher)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = cis.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
        } catch (GeneralSecurityException e) {
            throw new IOException("Decryption failed", e);
        }
    }
}
