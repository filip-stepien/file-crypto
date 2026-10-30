package xyz.cursedman.filecrypto.cryptors.classic.xor;

import lombok.experimental.SuperBuilder;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuperBuilder
public class XorCryptor extends Cryptor {

    private static final int BUFFER_SIZE = 8192;

    @Override
    public CryptorAlgorithm getAlgorithm() {
        return CryptorAlgorithm.XOR;
    }

    @Override
    public void encrypt(InputStream in, OutputStream out) throws IOException {
        xor(in, out);
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws IOException {
        xor(in, out); // same operation for XOR
    }

    private void xor(InputStream in, OutputStream out) throws IOException {
        XorCryptorKey xorKey = (XorCryptorKey) getKey();
        byte[] keyBytes = xorKey.getKey();
        int keyLen = keyBytes.length;
        if (keyLen == 0) throw new IllegalArgumentException("XOR key cannot be empty");

        byte[] keyStream = new byte[BUFFER_SIZE + keyLen];
        for (int i = 0; i < keyStream.length; i++) {
            keyStream[i] = keyBytes[i % keyLen];
        }

        byte[] buffer = new byte[BUFFER_SIZE];
        int keyOffset = 0;
        int read;

        while ((read = in.read(buffer)) != -1) {
            for (int i = 0; i < read; i++) {
                buffer[i] ^= keyStream[keyOffset + i];
            }

            keyOffset = (keyOffset + read) % keyLen;
            out.write(buffer, 0, read);
        }
    }
}
