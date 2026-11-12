module xyz.cursedman.filecrypto {
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires static lombok;
    requires java.desktop;

    opens xyz.cursedman.filecrypto.controllers to javafx.fxml;
    opens xyz.cursedman.filecrypto.controls to javafx.fxml;

    exports xyz.cursedman.filecrypto;
}