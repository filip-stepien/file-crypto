package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.AESCryptor.AESCryptor;
import xyz.cursedman.filecrypto.cryptors.AESCryptor.AESCryptorKey;
import xyz.cursedman.filecrypto.cryptors.CaesarCryptor.CaesarCryptor;
import xyz.cursedman.filecrypto.cryptors.CaesarCryptor.CaesarCryptorKey;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.keys.KeyInputType;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class CaesarKeyCreator implements KeyCreator {

    @Override
    public Collection<KeyInputField> getKeyInputFields() {
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


    @Override
    public CryptorKey createKey(Map<String, String> fieldValues) {
        int shift = Integer.parseInt(fieldValues.get("shift"));

        return CaesarCryptorKey.builder()
                .shift(shift)
                .build();
    }

    @Override
    public Collection<KeyInputField> getKeyGeneratorFields() {
        return List.of();
    }

    @Override
    public CryptorKey generateCryptorKey(Map<String, String> fieldValues) {
        SecureRandom random = new SecureRandom();
        return createKey(
                Map.of(
                        "shift", String.valueOf(
                                random.nextInt(Byte.MAX_VALUE - Byte.MIN_VALUE + 1))
                )
        );
    }

    @Override
    public Cryptor createCryptor(CryptorKey key) {
        if (key.getClass() != CaesarCryptorKey.class) {
            throw new RuntimeException("Invalid key type: " + key.getClass());
        }

        return CaesarCryptor.builder().key(key).build();
    }
}
