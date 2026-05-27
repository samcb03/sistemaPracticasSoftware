package uv.lis.GUI.cell;

import java.util.function.Consumer;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import uv.lis.logic.dto.Expedient;

public class ActionTableCell extends TableCell<Expedient, Void> {

    private static final String OPEN_BUTTON_LABEL = "Abrir";

    private final Button openButton;
    private final Consumer<Expedient> openDocumentHandler;

    public ActionTableCell(Consumer<Expedient> openDocumentHandler) {
        this.openDocumentHandler = openDocumentHandler;
        this.openButton = new Button(OPEN_BUTTON_LABEL);
        this.openButton.setOnAction(event -> notifyOpenRequested());
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : openButton);
    }

    private void notifyOpenRequested() {
        Expedient expedient = getTableView().getItems().get(getIndex());
        openDocumentHandler.accept(expedient);
    }
}