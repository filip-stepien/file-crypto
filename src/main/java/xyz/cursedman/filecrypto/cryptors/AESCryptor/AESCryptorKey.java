package xyz.cursedman.filecrypto.cryptors.AESCryptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Getter
@RequiredArgsConstructor
public class AESCryptorKey implements CryptorKey {

    private final SecretKey secretKey;

    public static AESCryptorKey fromBytes(byte[] keyBytes) {
        return new AESCryptorKey(new SecretKeySpec(keyBytes, "AES"));
    }

    public byte[] getEncoded() {
        return secretKey.getEncoded();
    }
}