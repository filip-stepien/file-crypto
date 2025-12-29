package xyz.cursedman.filecrypto.cryptors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CryptorAlgorithm {

    CAESAR("Caesar Cipher"),
    XOR("XOR Cipher"),
    AES("AES");

    private final String algorithmName;

    public static CryptorAlgorithm fromAlgorithmName(String text) {
        return Arrays.stream(values())
            .filter(algorithm -> algorithm.getAlgorithmName().equals(text))
            .findFirst()
            .orElseThrow(() ->
                new IllegalArgumentException("Cannot map algorithm name to enum constant: " + text)
            );
    }
}
