package xyz.cursedman.filecrypto.keys;

import xyz.cursedman.filecrypto.cryptors.CryptorKey;

import java.util.Map;

/*
 * Key creator for algorithms with separate encryption (public) and decryption (private) keys.
 * Encryption key comes from GUI fields, decryption key from a single text input.
 */
public interface AsymmetricKeyCreator extends KeyCreator {

    CryptorKey createEncryptionKey(Map<String, String> fieldValues);

    CryptorKey createDecryptionKey(String keyInput);

    /*
     * Field values (fieldId -> value) that recreate the encryption part of the key
     */
    Map<String, String> toEncryptionFieldValues(CryptorKey key);

    /*
     * Text accepted by createDecryptionKey that recreates the decryption part of the key
     */
    String toDecryptionKeyInput(CryptorKey key);

    @Override
    default CryptorKey createKey(Map<String, String> fieldValues) {
        return createEncryptionKey(fieldValues);
    }

    @Override
    default CryptorKey createKey(String keyInput) {
        return createDecryptionKey(keyInput);
    }
}
