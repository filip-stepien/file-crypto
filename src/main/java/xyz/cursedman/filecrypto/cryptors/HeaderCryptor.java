package xyz.cursedman.filecrypto.cryptors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.cursedman.filecrypto.exceptions.InvalidEncryptedHeaderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RequiredArgsConstructor
public class HeaderCryptor extends Cryptor {

    private final Cryptor cryptor;

    @Getter
    private static final String magicString = "FILE-CRYPTO";

    @Getter
    private static final byte[] magicBytes = magicString.getBytes(StandardCharsets.UTF_8);

    public void getHeader() {
    }

    @Override
    public void encrypt(InputStream in, OutputStream out) throws IOException {
        out.write(magicBytes);
        out.flush();
        cryptor.encrypt(in, out);
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws IOException {
        byte[] actualHeader = new byte[magicBytes.length];
        int read = in.read(actualHeader);

        if (read != magicBytes.length || !Arrays.equals(magicBytes, actualHeader)) {
            throw new InvalidEncryptedHeaderException();
        }

        cryptor.decrypt(in, out);
    }
}

