package xyz.cursedman.filecrypto.cryptors.aead;

import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public abstract class AEADCryptor extends Cryptor {

    private static final int BUFFER_SIZE = 4096;

    protected AEADCryptor(CryptorKey key) {
        super(key);
    }

    protected abstract String getTransformation();

    protected abstract int getNonceSize();

    protected abstract AlgorithmParameterSpec createParameterSpec(byte[] nonce);

    protected abstract SecretKey getSecretKey();

    @Override
    public void encrypt(InputStream in, OutputStream out) throws IOException {
        byte[] nonce = new byte[getNonceSize()];
        new SecureRandom().nextBytes(nonce);
        out.write(nonce);

        try {
            process(createCipher(Cipher.ENCRYPT_MODE, nonce), in, out);
        } catch (GeneralSecurityException e) {
            throw new IOException("Encryption failed", e);
        }
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws IOException {
        byte[] nonce = new byte[getNonceSize()];
        new DataInputStream(in).readFully(nonce);

        try {
            process(createCipher(Cipher.DECRYPT_MODE, nonce), in, out);
        } catch (GeneralSecurityException e) {
            throw new IOException("Decryption failed: wrong key or corrupted file", e);
        }
    }

    private Cipher createCipher(int mode, byte[] nonce) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(getTransformation());
        cipher.init(mode, getSecretKey(), createParameterSpec(nonce));
        return cipher;
    }

    private void process(Cipher cipher, InputStream in, OutputStream out) throws IOException, GeneralSecurityException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer)) != -1) {
            byte[] chunk = cipher.update(buffer, 0, read);
            if (chunk != null) {
                out.write(chunk);
            }
        }
        out.write(cipher.doFinal());
        out.flush();
    }
}
