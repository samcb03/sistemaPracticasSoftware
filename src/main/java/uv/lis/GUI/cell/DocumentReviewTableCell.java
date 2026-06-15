package uv.lis.GUI.cell;

import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;
import static uv.lis.logic.utils.InputValidator.STATUS_REJECTED;

import java.util.function.BiConsumer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

import uv.lis.logic.dto.Expedient;

public class DocumentReviewTableCell extends TableCell<Expedient, Void> {

    private static final String VALIDATE_BUTTON_LABEL = "✅";
    private static final String REJECT_BUTTON_LABEL = "❌";
    private static final double BUTTONS_SPACING = 5.0;

    private final Button validateButton;
    private final Button rejectButton;
    private final HBox buttonsContainer;
    private final BiConsumer<Expedient, Integer> reviewHandler;

    public DocumentReviewTableCell(BiConsumer<Expedient, Integer> reviewHandler) {
        this.reviewHandler = reviewHandler;
        this.validateButton = new Button(VALIDATE_BUTTON_LABEL);
        this.rejectButton = new Button(REJECT_BUTTON_LABEL);
        this.validateButton.setOnAction(event -> notifyReview(STATUS_ASSIGNED));
        this.rejectButton.setOnAction(event -> notifyReview(STATUS_REJECTED));
        this.buttonsContainer = new HBox(BUTTONS_SPACING, validateButton, rejectButton);
        this.buttonsContainer.setAlignment(Pos.CENTER);
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            Expedient expedient = getTableView().getItems().get(getIndex());
            boolean isValidated = expedient.isValidated();
            validateButton.setDisable(isValidated);
            rejectButton.setDisable(isValidated);
            setGraphic(buttonsContainer);
        }
    }

    private void notifyReview(int idStatus) {
        Expedient expedient = getTableView().getItems().get(getIndex());
        reviewHandler.accept(expedient, idStatus);
    }
}