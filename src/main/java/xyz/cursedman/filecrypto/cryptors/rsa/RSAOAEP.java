package xyz.cursedman.filecrypto.cryptors.rsa;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.spec.MGF1ParameterSpec;

final class RSAOAEP {

    private static final String TRANSFORMATION = "RSA/ECB/OAEPPadding";

    private static final int HASH_SIZE_BYTES = 32; // SHA-256

    // Explicit spec: the "OAEPWithSHA-256AndMGF1Padding" string alone would use SHA-1 for MGF1
    private static final OAEPParameterSpec PARAMETERS = new OAEPParameterSpec(
        "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
    );

    private RSAOAEP() {}

    static Cipher createCipher(int mode, Key key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode, key, PARAMETERS);
        return cipher;
    }

    static int maxPlainBlock(int modulusBytes) {
        return modulusBytes - 2 * HASH_SIZE_BYTES - 2;
    }
}
