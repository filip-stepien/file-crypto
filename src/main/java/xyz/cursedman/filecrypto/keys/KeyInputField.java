package xyz.cursedman.filecrypto.keys;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KeyInputField {
    private final String id;
    private final String label;
    private final String description;
    KeyInputType type;
    private String defaultValue;
}