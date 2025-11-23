package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.cryptors.XorCryptor.XorCryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.keys.KeyInputType;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class XorKeyCreator implements KeyCreator {

    @Override
    public String getAlgorithmName() {
        return "Xor cipher";
    }

    @Override
    public List<KeyInputField> getKeyInputFields() {
        return List.of(
                KeyInputField.builder()
                        .type(KeyInputType.HEX)
                        .label("key")
                        .id("key")
                        .description("key as hex")
                        .build()
        );
    }

    @Override
    public CryptorKey createKey(Map<String, String> fieldValues) throws Exception {
        byte[] bytes = HexFormat.of().parseHex(fieldValues.get("key"));
        return XorCryptorKey.builder()
                .key(bytes)
                .build();
    }

    @Override
    public CryptorKey generateCryptorKey() {
        int keyLength = 16; // default key length in bytes
        byte[] randomKey = new byte[keyLength];

        SecureRandom random = new SecureRandom();
        random.nextBytes(randomKey);

        return XorCryptorKey.builder()
                .key(randomKey)
                .build();
    }
}