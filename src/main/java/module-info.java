module xyz.cursedman.filecrypto {
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.javafx;

    opens xyz.cursedman.filecrypto to javafx.fxml;
    exports xyz.cursedman.filecrypto;
}