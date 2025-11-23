package xyz.cursedman.filecrypto.keys;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KeyInputField {
    private final String id;
    private final String label;
    KeyInputType type;
    private String defaultValue;
}
