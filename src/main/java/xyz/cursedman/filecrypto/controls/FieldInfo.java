package xyz.cursedman.filecrypto.controls;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class FieldInfo {

    private final Label label = new Label();

    private final Label description = new Label();

    @Getter
    private FieldControl fieldControl;

    private FieldInfo() {}

    public Node getNode() {
        if (fieldControl == null) {
            throw new RuntimeException("Could not create field info because field control is not set.");
        }

        label.setStyle("-fx-font-weight: bold;");
        description.setStyle("-fx-padding: 0 0 5 0; -fx-text-fill: gray;");

        VBox vBox = new VBox();
        vBox.getChildren().addAll(label, description, fieldControl.getNode());

        return vBox;
    }

    public static class FieldInfoBuilder {
        private String labelText;
        private String descriptionText;
        private FieldControl fieldControl;

        public FieldInfoBuilder label(String label) {
            this.labelText = label;
            return this;
        }

        public FieldInfoBuilder description(String description) {
            this.descriptionText = description;
            return this;
        }

        public FieldInfoBuilder fieldControl(FieldControl fieldControl) {
            this.fieldControl = fieldControl;
            return this;
        }

        public FieldInfo build() {
            FieldInfo info = new FieldInfo();

            if (labelText != null) {
                info.label.setText(labelText);
            }

            if (descriptionText != null) {
                info.description.setText(descriptionText);
            }

            info.fieldControl = fieldControl;

            return info;
        }
    }

    public static FieldInfoBuilder builder() {
        return new FieldInfoBuilder();
    }
}
