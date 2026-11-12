package xyz.cursedman.filecrypto.keys.impl;

import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.cryptors.rsa.RSACryptorKey;
import xyz.cursedman.filecrypto.exceptions.InvalidKeyException;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.keys.KeyInputType;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class RSAKeys {

    public static final String PUBLIC_KEY_FIELD = "publicKeyHex";

    private static final String KEY_SIZE_FIELD = "keySizeBits";

    private static final List<Integer> ALLOWED_KEY_SIZE_BITS = List.of(2048, 3072, 4096);

    public List<KeyInputField> getKeyInputFields() {
        return List.of(
            KeyInputField.builder()
                .id(PUBLIC_KEY_FIELD)
                .label("RSA Public Key (Hex)")
                .description("X.509 DER in hex. Decrypt with the matching private key.")
                .type(KeyInputType.HEX)
                .placeholder("Generate a key pair below...")
                .build()
        );
    }

    public RSACryptorKey createEncryptionKey(Map<String, String> fieldValues) {
        String hex = fieldValues.get(PUBLIC_KEY_FIELD);

        if (hex == null || hex.isBlank()) {
            throw new IllegalArgumentException("RSA public key is required.");
        }

        return RSACryptorKey.fromPublicBytes(parseHex(hex));
    }

    public RSACryptorKey createDecryptionKey(String keyInput) {
        if (keyInput == null || keyInput.isBlank()) {
            throw new IllegalArgumentException("RSA private key is required.");
        }

        return RSACryptorKey.fromPrivateBytes(parseHex(keyInput));
    }

    public Map<String, String> toEncryptionFieldValues(CryptorKey key) {
        return Map.of(PUBLIC_KEY_FIELD, HexFormat.of().formatHex(asRSAKey(key).requirePublicKey().getEncoded()));
    }

    public String toDecryptionKeyInput(CryptorKey key) {
        return HexFormat.of().formatHex(asRSAKey(key).requirePrivateKey().getEncoded());
    }

    public List<KeyInputField> getKeyGeneratorFields() {
        return List.of(
            KeyInputField.builder()
                .id(KEY_SIZE_FIELD)
                .label("Key size bits")
                .description("RSA modulus size.")
                .type(KeyInputType.SELECTION)
                .defaultValue("2048")
                .options(ALLOWED_KEY_SIZE_BITS.stream().map(Object::toString).toList())
                .build()
        );
    }

    public RSACryptorKey generateKey(Map<String, String> fieldValues) {
        int keySizeBits = Integer.parseInt(fieldValues.get(KEY_SIZE_FIELD));
        if (!ALLOWED_KEY_SIZE_BITS.contains(keySizeBits)) {
            throw new IllegalArgumentException("RSA key size bits " + keySizeBits + " is not valid.");
        }

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySizeBits, new SecureRandom());
            KeyPair pair = generator.generateKeyPair();
            return new RSACryptorKey(pair.getPublic(), pair.getPrivate());
        } catch (Exception e) {
            throw new RuntimeException("RSA key generation failed", e);
        }
    }

    public RSACryptorKey asRSAKey(CryptorKey key) {
        if (!(key instanceof RSACryptorKey rsaKey)) {
            throw new RuntimeException("Invalid key type: " + key.getClass());
        }
        return rsaKey;
    }

    private static byte[] parseHex(String hex) {
        try {
            return HexFormat.of().parseHex(hex.replaceAll("\\s", ""));
        } catch (IllegalArgumentException e) {
            throw new InvalidKeyException("RSA key must be hex.", e);
        }
    }
}
