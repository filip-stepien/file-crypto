package xyz.cursedman.filecrypto.cryptors.XorCryptor;

import lombok.experimental.SuperBuilder;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuperBuilder
public class XorCryptor extends Cryptor {

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

        int data;
        int i = 0;
        while ((data = in.read()) != -1) {
            out.write(data ^ keyBytes[i % keyLen]);
            i++;
        }
    }
}
