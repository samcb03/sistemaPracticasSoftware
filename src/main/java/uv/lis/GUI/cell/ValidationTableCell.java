package uv.lis.GUI.cell;

import java.util.function.BiConsumer;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;

import uv.lis.logic.dto.Expedient;

public class ValidationTableCell extends TableCell<Expedient, Void> {

    private final CheckBox validationCheckBox;
    private final BiConsumer<Expedient, CheckBox> validationToggleHandler;

    public ValidationTableCell(BiConsumer<Expedient, CheckBox> validationToggleHandler) {
        this.validationToggleHandler = validationToggleHandler;
        this.validationCheckBox = new CheckBox();
        this.validationCheckBox.setOnAction(event -> notifyValidationToggle());
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            Expedient expedient = getTableView().getItems().get(getIndex());
            validationCheckBox.setSelected(expedient.getIsValidated());
            setGraphic(validationCheckBox);
        }
    }

    private void notifyValidationToggle() {
        Expedient expedient = getTableView().getItems().get(getIndex());
        validationToggleHandler.accept(expedient, validationCheckBox);
    }
}