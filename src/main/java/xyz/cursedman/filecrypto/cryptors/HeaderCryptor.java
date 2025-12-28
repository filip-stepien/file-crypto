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
    private static final String headerString = "FILE-CRYPTO";

    @Getter
    private static final byte[] headerBytes = headerString.getBytes(StandardCharsets.UTF_8);

    @Override
    public void encrypt(InputStream in, OutputStream out) throws IOException {
        out.write(headerBytes);
        out.flush();
        cryptor.encrypt(in, out);
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws IOException {
        byte[] actualHeader = new byte[headerBytes.length];
        int read = in.read(actualHeader);

        if (read != headerBytes.length || !Arrays.equals(headerBytes, actualHeader)) {
            throw new InvalidEncryptedHeaderException();
        }

        cryptor.decrypt(in, out);
    }
}

