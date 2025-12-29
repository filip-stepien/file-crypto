package xyz.cursedman.filecrypto.cryptors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class Cryptor {

    private CryptorKey key;

    public CryptorAlgorithm getAlgorithmName() {
        return null;
    }

    public abstract void encrypt(InputStream in, OutputStream out) throws IOException;

    public abstract void decrypt(InputStream in, OutputStream out) throws IOException;
}
