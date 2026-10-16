package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.aes.AESCryptorKey;
import xyz.cursedman.filecrypto.cryptors.aes.AESCryptor;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class AESKeyCreator implements KeyCreator {

    private final AESKeys aesKeys = new AESKeys();

    @Override
    public List<KeyInputField> getKeyInputFields() {
        return aesKeys.getKeyInputFields();
    }

    @Override
    public CryptorKey createKey(Map<String, String> fieldValues) {
        return aesKeys.createKey(fieldValues);
    }

    @Override
    public CryptorKey createKey(String keyInput) {
        return aesKeys.createKey(keyInput);
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
        return aesKeys.getKeyGeneratorFields();
    }

    @Override
    public CryptorKey generateCryptorKey(Map<String, String> fieldValues) {
        return aesKeys.generateKey(fieldValues);
    }
}
