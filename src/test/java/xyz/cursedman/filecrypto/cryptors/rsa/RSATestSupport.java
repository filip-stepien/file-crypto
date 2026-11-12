package xyz.cursedman.filecrypto.cryptors.rsa;

import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.keys.impl.RSAKeys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

final class RSATestSupport {

    static final RSAKeys RSA_KEYS = new RSAKeys();

    private RSATestSupport() {}

    static RSACryptorKey generateKeyPair() {
        return RSA_KEYS.generateKey(Map.of("keySizeBits", "2048"));
    }

    /*
     * Round-trips the pair through its text form, like the GUI does
     */
    static RSACryptorKey publicOnly(RSACryptorKey pair) {
        return RSA_KEYS.createEncryptionKey(RSA_KEYS.toEncryptionFieldValues(pair));
    }

    static RSACryptorKey privateOnly(RSACryptorKey pair) {
        return RSA_KEYS.createDecryptionKey(RSA_KEYS.toDecryptionKeyInput(pair));
    }

    static byte[] encrypt(Cryptor cryptor, byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cryptor.encrypt(new ByteArrayInputStream(data), out);
        return out.toByteArray();
    }

    static byte[] decrypt(Cryptor cryptor, byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cryptor.decrypt(new ByteArrayInputStream(data), out);
        return out.toByteArray();
    }
}
