package xyz.cursedman.filecrypto.cryptors;

import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class AsymmetricCryptor extends Cryptor {

    protected AsymmetricCryptor(AsymmetricCryptorKey key) {
        super(key);
    }

    protected AsymmetricCryptorKey getAsymmetricKey() {
        return (AsymmetricCryptorKey) getKey();
    }

    protected PublicKey getEncryptionKey() {
        return getAsymmetricKey().requirePublicKey();
    }

    protected PrivateKey getDecryptionKey() {
        return getAsymmetricKey().requirePrivateKey();
    }
}
