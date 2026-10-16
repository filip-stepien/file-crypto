package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.aead.chacha20.ChaCha20Cryptor;
import xyz.cursedman.filecrypto.cryptors.aead.chacha20.ChaCha20CryptorKey;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.keys.KeyInputType;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class ChaCha20KeyCreator implements KeyCreator {

    private static final int KEY_SIZE_BITS = ChaCha20CryptorKey.KEY_SIZE_BYTES * 8;

    @Override
    public List<KeyInputField> getKeyInputFields() {
        return List.of(
            KeyInputField.builder()
                .id("keyHex")
                .label("ChaCha20 Key (Hex)")
                .description("64 hex characters (256 bits).")
                .type(KeyInputType.HEX)
                .build()
        );
    }

    @Override
    public CryptorKey createKey(Map<String, String> fieldValues) {
        String hex = fieldValues.get("keyHex");

        if (hex == null || hex.trim().isEmpty()) {
            throw new IllegalArgumentException("ChaCha20 key is required.");
        }

        return createKey(hex);
    }

    @Override
    public CryptorKey createKey(String keyInput) {
        return ChaCha20CryptorKey.fromBytes(HexFormat.of().parseHex(keyInput.trim()));
    }

    @Override
    public Cryptor createCryptor(CryptorKey key) {
        if (key.getClass() != ChaCha20CryptorKey.class) {
            throw new RuntimeException("Invalid key type: " + key.getClass());
        }

        return new ChaCha20Cryptor((ChaCha20CryptorKey) key);
    }

    @Override
    public Collection<KeyInputField> getKeyGeneratorFields() {
        return List.of(
            KeyInputField.builder()
                .id("keySizeBits")
                .label("key size bits")
                .description("ChaCha20 supports only 256-bit keys")
                .type(KeyInputType.SELECTION)
                .defaultValue(String.valueOf(KEY_SIZE_BITS))
                .options(List.of(String.valueOf(KEY_SIZE_BITS)))
                .build()
        );
    }

    @Override
    public CryptorKey generateCryptorKey(Map<String, String> fieldValues) {
        byte[] keyBytes = new byte[ChaCha20CryptorKey.KEY_SIZE_BYTES];
        new SecureRandom().nextBytes(keyBytes);
        return ChaCha20CryptorKey.fromBytes(keyBytes);
    }
}
