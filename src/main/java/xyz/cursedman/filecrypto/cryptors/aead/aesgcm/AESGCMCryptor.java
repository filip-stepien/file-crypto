package xyz.cursedman.filecrypto.cryptors.aead.aesgcm;

import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.cryptors.aead.AEADCryptor;
import xyz.cursedman.filecrypto.cryptors.aes.AESCryptorKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.spec.AlgorithmParameterSpec;

public class AESGCMCryptor extends AEADCryptor {

    private static final int NONCE_SIZE = 12;
    private static final int TAG_SIZE_BITS = 128;

    public AESGCMCryptor(AESCryptorKey key) {
        super(key);
    }

    @Override
    public CryptorAlgorithm getAlgorithm() {
        return CryptorAlgorithm.AES_GCM;
    }

    @Override
    protected String getTransformation() {
        return "AES/GCM/NoPadding";
    }

    @Override
    protected int getNonceSize() {
        return NONCE_SIZE;
    }

    @Override
    protected AlgorithmParameterSpec createParameterSpec(byte[] nonce) {
        return new GCMParameterSpec(TAG_SIZE_BITS, nonce);
    }

    @Override
    protected SecretKey getSecretKey() {
        return ((AESCryptorKey) getKey()).getSecretKey();
    }
}
