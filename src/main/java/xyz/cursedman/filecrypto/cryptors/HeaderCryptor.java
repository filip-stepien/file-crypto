package xyz.cursedman.filecrypto.cryptors;

import lombok.RequiredArgsConstructor;
import xyz.cursedman.filecrypto.exceptions.InvalidEncryptedHeaderException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RequiredArgsConstructor
public class HeaderCryptor extends Cryptor {

    private final Cryptor cryptor;

    public record Header(String algorithmName) {}

    public static final String MAGIC_STRING = "FILE-CRYPTO";

    public static final byte[] MAGIC_BYTES = MAGIC_STRING.getBytes(StandardCharsets.UTF_8);

    private void writeHeader(OutputStream out) throws IOException {
        byte[] algorithmNameBytes =
            cryptor.getAlgorithm().getName().getBytes(StandardCharsets.UTF_8);

        DataOutputStream dataOut = new DataOutputStream(out);

        dataOut.write(MAGIC_BYTES);
        dataOut.writeInt(algorithmNameBytes.length);
        dataOut.write(algorithmNameBytes);
        dataOut.flush();
    }

    public static Header readHeader(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);

        byte[] actualMagic = new byte[MAGIC_BYTES.length];
        dataIn.readFully(actualMagic);

        if (!Arrays.equals(MAGIC_BYTES, actualMagic)) {
            throw new InvalidEncryptedHeaderException("Invalid magic header");
        }

        int algorithmNameLength = dataIn.readInt();
        if (algorithmNameLength <= 0 || algorithmNameLength > CryptorAlgorithm.MAX_ALGORITHM_NAME_LENGTH) {
            throw new InvalidEncryptedHeaderException("Invalid algorithm name length");
        }

        byte[] algorithmNameBytes = new byte[algorithmNameLength];
        dataIn.readFully(algorithmNameBytes);

        String algorithmName = new String(algorithmNameBytes, StandardCharsets.UTF_8);

        return new Header(algorithmName);
    }

    @Override
    public void encrypt(InputStream in, OutputStream out) throws IOException {
        writeHeader(out);
        cryptor.encrypt(in, out);
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws IOException {
        cryptor.decrypt(in, out);
    }
}


