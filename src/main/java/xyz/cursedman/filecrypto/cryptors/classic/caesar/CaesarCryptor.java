package xyz.cursedman.filecrypto.cryptors.classic.caesar;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuperBuilder
public class CaesarCryptor extends Cryptor {

    private static final int BUFFER_SIZE = 8192;

    @Override
    public CryptorAlgorithm getAlgorithm() {
        return CryptorAlgorithm.CAESAR;
    }

    @Override
    public void encrypt(InputStream in, OutputStream out) throws IOException {
        shift(in, out, ((CaesarCryptorKey) getKey()).getShift());
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws IOException {
        shift(in, out, -((CaesarCryptorKey) getKey()).getShift());
    }

    private void shift(InputStream in, OutputStream out, int shift) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;

        while ((read = in.read(buffer)) != -1) {
            for (int i = 0; i < read; i++) {
                buffer[i] = (byte) (buffer[i] + shift);
            }

            out.write(buffer, 0, read);
        }
    }
}
