package xyz.cursedman.filecrypto.keys;

import xyz.cursedman.filecrypto.cryptors.CryptorKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface KeyCreator {

    static String getAlgorithmName() {
        return null;
    }

    Collection<KeyInputField> getKeyInputFields();

    /*
     * Create key based on GUI-provided values
     * map: fieldId -> user value
     */
    CryptorKey createKey(Map<String, String> fieldValues);

    /*
     * Create a fully random key (GUI may use a "Generate" button)
     */
    CryptorKey generateCryptorKey();

}
