package xyz.cursedman.filecrypto.keys;

import xyz.cursedman.filecrypto.cryptors.CryptorKey;

import java.util.List;
import java.util.Map;

public interface KeyCreator {
    String getAlgorithmName();

    List<KeyInputField> getKeyInputFields();

    CryptorKey getCryptorKey();

    /*
    * Create key based on GUI-provided values
    * map: fieldId -> user value
    */
    CryptorKey createKey(Map<String, String> fieldValues) throws Exception;

    /*
    * Create a fully random key (GUI may use a "Generate" button)
    */
    CryptorKey generateCryptorKey();

}
