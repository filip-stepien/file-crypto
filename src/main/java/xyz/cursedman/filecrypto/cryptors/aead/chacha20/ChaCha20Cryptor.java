package xyz.cursedman.filecrypto.cryptors.aead.chacha20;

import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.cryptors.aead.AEADCryptor;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.spec.AlgorithmParameterSpec;

public class ChaCha20Cryptor extends AEADCryptor {

    private static final int NONCE_SIZE = 12;

    public ChaCha20Cryptor(ChaCha20CryptorKey key) {
        super(key);
    }

    @Override
    public CryptorAlgorithm getAlgorithm() {
        return CryptorAlgorithm.CHACHA20_POLY1305;
    }

    @Override
    protected String getTransformation() {
        return "ChaCha20-Poly1305";
    }

    @Override
    protected int getNonceSize() {
        return NONCE_SIZE;
    }

    @Override
    protected AlgorithmParameterSpec createParameterSpec(byte[] nonce) {
        return new IvParameterSpec(nonce);
    }

    @Override
    protected SecretKey getSecretKey() {
        return ((ChaCha20CryptorKey) getKey()).getSecretKey();
    }
}
