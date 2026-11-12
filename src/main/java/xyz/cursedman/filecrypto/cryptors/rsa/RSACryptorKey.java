package xyz.cursedman.filecrypto.cryptors.rsa;

import lombok.Getter;
import xyz.cursedman.filecrypto.cryptors.AsymmetricCryptorKey;
import xyz.cursedman.filecrypto.exceptions.InvalidKeyException;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Getter
public class RSACryptorKey implements AsymmetricCryptorKey {

    private final PublicKey publicKey;

    private final PrivateKey privateKey;

    public RSACryptorKey(PublicKey publicKey, PrivateKey privateKey) {
        if (publicKey == null && privateKey == null) {
            throw new IllegalArgumentException("RSA key needs a public or a private part.");
        }
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public static RSACryptorKey fromPublicBytes(byte[] x509Bytes) {
        try {
            return new RSACryptorKey(keyFactory().generatePublic(new X509EncodedKeySpec(x509Bytes)), null);
        } catch (GeneralSecurityException e) {
            throw new InvalidKeyException("Invalid RSA public key (expected X.509 DER).", e);
        }
    }

    public static RSACryptorKey fromPrivateBytes(byte[] pkcs8Bytes) {
        try {
            KeyFactory keyFactory = keyFactory();
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(pkcs8Bytes));
            PublicKey publicKey = null;

            if (privateKey instanceof RSAPrivateCrtKey crtKey) {
                publicKey = keyFactory.generatePublic(
                    new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent())
                );
            }

            return new RSACryptorKey(publicKey, privateKey);
        } catch (GeneralSecurityException e) {
            throw new InvalidKeyException("Invalid RSA private key (expected PKCS#8 DER).", e);
        }
    }

    public int getModulusBytes() {
        RSAKey rsaKey = (RSAKey) (publicKey != null ? publicKey : privateKey);
        return (rsaKey.getModulus().bitLength() + 7) / 8;
    }

    private static KeyFactory keyFactory() throws GeneralSecurityException {
        return KeyFactory.getInstance("RSA");
    }
}
