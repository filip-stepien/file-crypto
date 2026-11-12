package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.cryptors.rsa.RSAOAEPCryptor;
import xyz.cursedman.filecrypto.keys.AsymmetricKeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RSAOAEPKeyCreator implements AsymmetricKeyCreator {

    private final RSAKeys rsaKeys = new RSAKeys();

    @Override
    public List<KeyInputField> getKeyInputFields() {
        return rsaKeys.getKeyInputFields();
    }

    @Override
    public CryptorKey createEncryptionKey(Map<String, String> fieldValues) {
        return rsaKeys.createEncryptionKey(fieldValues);
    }

    @Override
    public CryptorKey createDecryptionKey(String keyInput) {
        return rsaKeys.createDecryptionKey(keyInput);
    }

    @Override
    public Map<String, String> toEncryptionFieldValues(CryptorKey key) {
        return rsaKeys.toEncryptionFieldValues(key);
    }

    @Override
    public String toDecryptionKeyInput(CryptorKey key) {
        return rsaKeys.toDecryptionKeyInput(key);
    }

    @Override
    public Cryptor createCryptor(CryptorKey key) {
        return new RSAOAEPCryptor(rsaKeys.asRSAKey(key));
    }

    @Override
    public Collection<KeyInputField> getKeyGeneratorFields() {
        return rsaKeys.getKeyGeneratorFields();
    }

    @Override
    public CryptorKey generateCryptorKey(Map<String, String> fieldValues) {
        return rsaKeys.generateKey(fieldValues);
    }
}
