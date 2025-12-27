package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.CaesarCryptor.CaesarCryptor;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.cryptors.XorCryptor.XorCryptor;
import xyz.cursedman.filecrypto.cryptors.XorCryptor.XorCryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.keys.KeyInputType;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class XorKeyCreator implements KeyCreator {

    public static String getAlgorithmName() {
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
                        .placeholder("Enter a hexadecimal key...")
                        .build()
        );
    }

    @Override
    public CryptorKey createKey(Map<String, String> fieldValues) {
        byte[] bytes = HexFormat.of().parseHex(fieldValues.get("key"));
        return XorCryptorKey.builder()
                .key(bytes)
                .build();
    }

    @Override
    public Collection<KeyInputField> getKeyGeneratorFields() {
        return List.of(
                KeyInputField.builder()
                        .id("length")
                        .label("length")
                        .defaultValue("10")
                        .description("key length")
                        .type(KeyInputType.NUMBER)
                        .placeholder("Enter a key length")
                        .build()
        );
    }


    @Override
    public CryptorKey generateCryptorKey(Map<String, String> fieldValues) {
        int keyLength = Integer.parseInt(fieldValues.get("length"));
        byte[] randomKey = new byte[keyLength];

        SecureRandom random = new SecureRandom();
        random.nextBytes(randomKey);

        return XorCryptorKey.builder()
                .key(randomKey)
                .build();
    }

    @Override
    public Cryptor createCryptor(CryptorKey key) {
        if (key.getClass() != XorCryptorKey.class) {
            throw new RuntimeException("Invalid key type: " + key.getClass());
        }

        return XorCryptor.builder().key(key).build();
    }
}