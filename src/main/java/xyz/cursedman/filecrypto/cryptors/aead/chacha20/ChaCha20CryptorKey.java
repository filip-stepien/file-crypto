package xyz.cursedman.filecrypto.cryptors.aead.chacha20;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Getter
@RequiredArgsConstructor
public class ChaCha20CryptorKey implements CryptorKey {

    public static final int KEY_SIZE_BYTES = 32;

    private final SecretKey secretKey;

    public static ChaCha20CryptorKey fromBytes(byte[] keyBytes) {
        if (keyBytes.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException("ChaCha20 key must be " + KEY_SIZE_BYTES + " bytes, got " + keyBytes.length);
        }
        return new ChaCha20CryptorKey(new SecretKeySpec(keyBytes, "ChaCha20"));
    }
}
