package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.AESCryptor.AESCryptorKey;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.keys.KeyInputType;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class AESKeyCreator implements KeyCreator {

    private final int keySizeBits; // 128 / 192 / 256

    public AESKeyCreator(int keySizeBits) {
        this.keySizeBits = keySizeBits;
    }

    @Override
    public String getAlgorithmName() {
        return "AES-" + keySizeBits;
    }
    
    @Override
    public List<KeyInputField> getKeyInputFields() {
        return List.of(
                KeyInputField.builder()
                        .id("keyHex")
                        .label("AES Key (Hex)")
                        .description("Enter " + (keySizeBits / 8 * 2) + " hex characters.")
                        .type(KeyInputType.HEX)
                        .build()
        );
    }

    @Override
    public CryptorKey createKey(Map<String, String> fieldValues) {
        String hex = fieldValues.get("keyHex");
        if (hex == null || hex.trim().isEmpty()) {
            throw new IllegalArgumentException("AES key is required.");
        }
        byte[] keyBytes = HexFormat.of().parseHex(hex);

        if (keyBytes.length * 8 != keySizeBits) {
            throw new IllegalArgumentException("AES key must be " + keySizeBits + " bits.");
        }
        return AESCryptorKey.fromBytes(keyBytes);
    }

    @Override
    public CryptorKey generateCryptorKey() {
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
