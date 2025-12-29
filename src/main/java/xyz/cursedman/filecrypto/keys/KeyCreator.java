package xyz.cursedman.filecrypto.keys;

import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;

import java.util.Collection;
import java.util.Map;

public interface KeyCreator {

    Collection<KeyInputField> getKeyInputFields();

    /*
     * Create key based on GUI-provided values
     * map: fieldId -> user value
     */
    CryptorKey createKey(Map<String, String> fieldValues);

    Cryptor createCryptor(CryptorKey key);

    /*
     * fields that generateCryptorKey takes
     */
    Collection<KeyInputField> getKeyGeneratorFields();

    /*
     * Create a fully random key (GUI may use a "Generate" button)
     */
    CryptorKey generateCryptorKey(Map<String, String> fieldValues);

}
