package xyz.cursedman.filecrypto.controls;

import javafx.scene.control.Control;
import lombok.AllArgsConstructor;


public interface FieldControl {

    public abstract Control getControl();

    public abstract String getControlValue();

}
