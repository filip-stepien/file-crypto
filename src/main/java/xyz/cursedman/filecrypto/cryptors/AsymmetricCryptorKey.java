package xyz.cursedman.filecrypto.cryptors;

import xyz.cursedman.filecrypto.exceptions.InvalidKeyException;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface AsymmetricCryptorKey extends CryptorKey {

    /*
     * Key used for encryption, may be null when only the private part is known
     */
    PublicKey getPublicKey();

    /*
     * Key used for decryption, may be null when only the public part is known
     */
    PrivateKey getPrivateKey();

    default PublicKey requirePublicKey() {
        PublicKey publicKey = getPublicKey();
        if (publicKey == null) {
            throw new InvalidKeyException("Public key is required for encryption.");
        }
        return publicKey;
    }

    default PrivateKey requirePrivateKey() {
        PrivateKey privateKey = getPrivateKey();
        if (privateKey == null) {
            throw new InvalidKeyException("Private key is required for decryption.");
        }
        return privateKey;
    }
}
