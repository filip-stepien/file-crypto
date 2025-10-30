module xyz.cursedman.filecrypto {
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.javafx;
    requires static lombok;
    requires java.desktop;

    opens xyz.cursedman.filecrypto.controllers to javafx.fxml;

    exports xyz.cursedman.filecrypto;
}