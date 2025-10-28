module xyz.cursedman.filecrypto {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens xyz.cursedman.filecrypto to javafx.fxml;
    exports xyz.cursedman.filecrypto;
}