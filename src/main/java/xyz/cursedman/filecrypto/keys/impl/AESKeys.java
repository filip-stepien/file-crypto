package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.aes.AESCryptorKey;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.keys.KeyInputType;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class AESKeys {

    private static final List<Integer> ALLOWED_KEY_SIZE_BITS = List.of(
        128, 192, 256
    );

    public List<KeyInputField> getKeyInputFields() {
        return List.of(
            KeyInputField.builder()
                .id("keyHex")
                .label("AES Key (Hex)")
                .description("Hex characters.")
                .type(KeyInputType.HEX)
                .build()
        );
    }

    public AESCryptorKey createKey(Map<String, String> fieldValues) {
        String hex = fieldValues.get("keyHex");

        if (hex == null || hex.trim().isEmpty()) {
            throw new IllegalArgumentException("AES key is required.");
        }

        return createKey(hex);
    }

    public AESCryptorKey createKey(String keyInput) {
        return AESCryptorKey.fromBytes(HexFormat.of().parseHex(keyInput));
    }

    public List<KeyInputField> getKeyGeneratorFields() {
        return List.of(
            KeyInputField.builder()
                .id("keySizeBits")
                .label("key size bits")
                .description("Bits of key size in hex format")
                .type(KeyInputType.SELECTION)
                .defaultValue("256")
                .options(ALLOWED_KEY_SIZE_BITS.stream().map(Object::toString).toList())
                .placeholder("Bits in key")
                .build()
        );
    }

    public AESCryptorKey generateKey(Map<String, String> fieldValues) {
        int keySizeBits = Integer.parseInt(fieldValues.get("keySizeBits"));
        if (!ALLOWED_KEY_SIZE_BITS.contains(keySizeBits)) {
            throw new IllegalArgumentException("AES key size bits " + keySizeBits + "  is not valid.");
        }

        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(keySizeBits, new SecureRandom());
            SecretKey key = kg.generateKey();
            return new AESCryptorKey(key);
        } catch (Exception e) {
            throw new RuntimeException("AES key generation failed", e);
        }
    }
}
