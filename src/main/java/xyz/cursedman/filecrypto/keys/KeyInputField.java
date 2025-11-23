package xyz.cursedman.filecrypto.keys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class KeyInputField {
    private final String id;

    private final String label;

    private final String description;

    private final KeyInputType type;

    private final String defaultValue;

    private final String placeholder;

    // Used when type is selection
    private final List<String> options;
}