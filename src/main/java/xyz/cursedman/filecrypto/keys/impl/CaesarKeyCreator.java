package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.CaesarCryptor.CaesarCryptorKey;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.keys.KeyInputType;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;


@SuppressWarnings("unused")
public class CaesarKeyCreator implements KeyCreator {

    public static String getAlgorithmName() {
        return "Caesar Cipher";
    }

    @Override
    public CryptorKey createKey(Map<String, String> fieldValues) {
        int shift = Integer.parseInt(fieldValues.get("shift"));

        return CaesarCryptorKey.builder()
                .shift(shift)
                .build();
    }

    @Override
    public CryptorKey generateCryptorKey() {
        SecureRandom random = new SecureRandom();
        return createKey(
                Map.of(
                        "shift", String.valueOf(random.nextInt(Byte.MAX_VALUE - Byte.MIN_VALUE + 1))
                )
        );
    }

    public List<KeyInputField> getKeyInputFields() {
        return List.of(
                KeyInputField.builder()
                        .id("shift")
                        .label("shift")
                        .description("shift")
                        .type(KeyInputType.NUMBER)
                        .defaultValue("6")
                        .placeholder("Enter shift number...")
                        .build()
        );
    }

}
