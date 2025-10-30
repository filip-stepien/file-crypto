package xyz.cursedman.filecrypto.cryptors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("unused")
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
public abstract class Cryptor {

    final private CryptorKey key;

    public abstract void encrypt(InputStream in, OutputStream out) throws IOException;

    public abstract void decrypt(InputStream in, OutputStream out) throws IOException;
}
