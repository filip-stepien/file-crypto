package xyz.cursedman.filecrypto.cryptors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.impl.AESGCMKeyCreator;
import xyz.cursedman.filecrypto.keys.impl.AESKeyCreator;
import xyz.cursedman.filecrypto.keys.impl.CaesarKeyCreator;
import xyz.cursedman.filecrypto.keys.impl.ChaCha20KeyCreator;
import xyz.cursedman.filecrypto.keys.impl.XorKeyCreator;

import java.util.Arrays;
import java.util.Map;

@RequiredArgsConstructor
public enum CryptorAlgorithm {

    CAESAR("Caesar Cipher"),
    XOR("XOR Cipher"),
    AES("AES"),
    AES_GCM("AES-GCM"),
    CHACHA20_POLY1305("ChaCha20-Poly1305");

    @Getter
    private final String name;

    public static final int MAX_ALGORITHM_NAME_LENGTH = 128;

    private static final Map<CryptorAlgorithm, KeyCreator> KEY_CREATORS = Map.of(
        CAESAR, new CaesarKeyCreator(),
        XOR, new XorKeyCreator(),
        AES, new AESKeyCreator(),
        AES_GCM, new AESGCMKeyCreator(),
        CHACHA20_POLY1305, new ChaCha20KeyCreator()
    );

    public static KeyCreator getKeyCreator(CryptorAlgorithm algorithm) {
        return KEY_CREATORS.get(algorithm);
    }

    public static CryptorAlgorithm fromAlgorithmName(String text) {
        return Arrays.stream(values())
            .filter(algorithm -> algorithm.getName().equals(text))
            .findFirst()
            .orElseThrow(() ->
                new IllegalArgumentException("Cannot map algorithm name to enum constant: " + text)
            );
    }
}
