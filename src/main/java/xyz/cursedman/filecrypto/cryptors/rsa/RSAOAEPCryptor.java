package xyz.cursedman.filecrypto.cryptors.rsa;

import xyz.cursedman.filecrypto.cryptors.AsymmetricCryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

/*
 * Pure RSA-OAEP: input is split into blocks, every block is encrypted with RSA separately.
 */
public class RSAOAEPCryptor extends AsymmetricCryptor {

    public RSAOAEPCryptor(RSACryptorKey key) {
        super(key);
    }

    @Override
    public CryptorAlgorithm getAlgorithm() {
        return CryptorAlgorithm.RSA_OAEP;
    }

    private int getModulusBytes() {
        return ((RSACryptorKey) getKey()).getModulusBytes();
    }

    @Override
    public void encrypt(InputStream in, OutputStream out) throws IOException {
        try {
            Cipher cipher = RSAOAEP.createCipher(Cipher.ENCRYPT_MODE, getEncryptionKey());
            int blockSize = RSAOAEP.maxPlainBlock(getModulusBytes());

            byte[] block;
            while ((block = in.readNBytes(blockSize)).length > 0) {
                out.write(cipher.doFinal(block));
            }
            out.flush();
        } catch (GeneralSecurityException e) {
            throw new IOException("Encryption failed", e);
        }
    }

    @Override
    public void decrypt(InputStream in, OutputStream out) throws IOException {
        try {
            Cipher cipher = RSAOAEP.createCipher(Cipher.DECRYPT_MODE, getDecryptionKey());
            int blockSize = getModulusBytes();

            byte[] block;
            while ((block = in.readNBytes(blockSize)).length > 0) {
                if (block.length != blockSize) {
                    throw new IOException("Decryption failed: corrupted file (incomplete RSA block)");
                }
                out.write(cipher.doFinal(block));
            }
            out.flush();
        } catch (GeneralSecurityException e) {
            throw new IOException("Decryption failed: wrong key or corrupted file", e);
        }
    }
}
