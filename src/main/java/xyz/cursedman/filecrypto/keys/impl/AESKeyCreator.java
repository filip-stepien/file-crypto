package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.AESCryptor.AESCryptor;
import xyz.cursedman.filecrypto.cryptors.AESCryptor.AESCryptorKey;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.keys.KeyInputType;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class AESKeyCreator implements KeyCreator {

    static private final List<Integer> allowedKeySizeBits = List.of(
            128, 192, 256
    );

    @Override
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


    @Override
    public CryptorKey createKey(Map<String, String> fieldValues) {
        String hex = fieldValues.get("keyHex");

        if (hex == null || hex.trim().isEmpty()) {
            throw new IllegalArgumentException("AES key is required.");
        }
        byte[] keyBytes = HexFormat.of().parseHex(hex);

        return AESCryptorKey.fromBytes(keyBytes);
    }

    @Override
    public Cryptor createCryptor(CryptorKey key) {
        if (key.getClass() != AESCryptorKey.class) {
            throw new RuntimeException("Invalid key type: " + key.getClass());
        }

        return new AESCryptor((AESCryptorKey) key);
    }

    @Override
    public Collection<KeyInputField> getKeyGeneratorFields() {
        return List.of(
                KeyInputField.builder()
                        .id("keySizeBits")
                        .label("key size bits")
                        .description("Bits of key size in hex format")
                        .type(KeyInputType.SELECTION)
                        .defaultValue("256")
                        .options(
                                allowedKeySizeBits.stream().map(Object::toString).toList()
                        )
                        .placeholder("Bits in key")
                        .build()
        );
    }


    @Override
    public CryptorKey generateCryptorKey(Map<String, String> fieldValues) {
        int keySizeBits = Integer.parseInt(fieldValues.get("keySizeBits"));
        if (!allowedKeySizeBits.contains(keySizeBits)) {
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
