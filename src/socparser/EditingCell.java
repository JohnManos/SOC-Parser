package socparser;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

// EditingCell - for editing capability in a TableCell
public class EditingCell extends TableCell<ObservableList<String>, String> {
private TextField textField;

public EditingCell() {
}

@Override public void startEdit() {
    super.startEdit();

    if (textField == null) {
        createTextField();
    }
    setText(null);
    setGraphic(textField);
    textField.selectAll();
}

@Override public void cancelEdit() {
    super.cancelEdit();
    setText((String) getItem());
    setGraphic(null);
}

@Override public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);
    if (empty) {
        setText(null);
        setGraphic(null);
    } else {
        if (isEditing()) {
            if (textField != null) {
                textField.setText(getString());
            }
            setText(null);
            setGraphic(textField);
        } else {
            setText(getString());
            setGraphic(null);
        }
    }
}

private void createTextField() {
    textField = new TextField(getString());
    textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
    textField.setOnKeyReleased(new EventHandler<KeyEvent>() {                
        @Override public void handle(KeyEvent t) {
            if (t.getCode() == KeyCode.ENTER) {
                commitEdit(textField.getText());
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        }
    });

    textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
         @Override
         public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
             if (!newValue) {
                    commitEdit(textField.getText());
             }
         }
    });
}

private String getString() {
    return getItem() == null ? "" : getItem().toString();
}
} 