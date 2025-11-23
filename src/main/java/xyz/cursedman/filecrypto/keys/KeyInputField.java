package xyz.cursedman.filecrypto.keys;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
public class KeyInputField {
    private final String id;

    private final String label;

    private final String description;

    private KeyInputType type;
}