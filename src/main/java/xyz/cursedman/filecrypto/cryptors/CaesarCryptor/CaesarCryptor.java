package xyz.cursedman.filecrypto.cryptors.CaesarCryptor;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import xyz.cursedman.filecrypto.cryptors.Cryptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuperBuilder
public class CaesarCryptor extends Cryptor {

    @Override
    public void encrypt(InputStream in, OutputStream out) throws IOException {
        CaesarCryptorKey key = (CaesarCryptorKey) getKey();
        int shift = key.getShift();

        int data;
        while ((data = in.read()) != -1) {
            out.write((data + shift) & 0xFF); // byte-wise shift, wrap around 0-255
        }
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws IOException {
        CaesarCryptorKey key = (CaesarCryptorKey) getKey();
        int shift = key.getShift();

        int data;
        while ((data = in.read()) != -1) {
            out.write((data - shift) & 0xFF); // reverse shift, wrap around
        }
    }
}
