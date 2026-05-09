package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.PROFESSOR_ID_LENGTH;
import static uv.lis.logic.utils.InputValidator.validateExactLength;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

public class FXMLConsultProfessorController extends ValidationHandler {

    @FXML private Button buttonSearch;
    @FXML private Button buttonInactive;
    @FXML private Button buttonUpdate;
    @FXML private Button buttonBack;
    @FXML private TextField textFieldProfessorPersonnelNumber;
    @FXML private Label labelFirstName;
    @FXML private Label labelLastName;
    @FXML private Label labelCoordinator;
    @FXML private Label labelMessage;
    @FXML private GridPane gridPaneProfessorInfo;

    private ContextMenu contextMenuSuggestions;
    private ProfessorDAO professorDAO;
    private Professor currentProfessor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        professorDAO = new ProfessorDAO();
        contextMenuSuggestions = new ContextMenu();
        setupControls(labelMessage, buttonBack); 
        gridPaneProfessorInfo.setVisible(true);
        setupAutocomplete();
    }

    @FXML
    private void searchProfessor() {
        clearFields();
        String personnelNumber = textFieldProfessorPersonnelNumber.getText().trim();

        Optional<String> validationError = validateExactLength(
            personnelNumber, PROFESSOR_ID_LENGTH, "El número de personal");

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            try {
                int userId = professorDAO.getIdUserByProfessorPersonnelNumber(personnelNumber);
                currentProfessor = professorDAO.getProfessorById(userId);

                labelFirstName.setText(currentProfessor.getFirstName());
                labelLastName.setText(currentProfessor.getLastName());
                labelCoordinator.setText(currentProfessor.getIsCoordinator() ? "Coordinador" : "Profesor");

                gridPaneProfessorInfo.setVisible(true);
                buttonInactive.setDisable(false);
                buttonUpdate.setDisable(false);
                labelMessage.setText("");

            } catch (OperationException e) {
                showError(e.getMessage());
                gridPaneProfessorInfo.setVisible(false);
                buttonInactive.setDisable(true);
                buttonUpdate.setDisable(true);
            }
        }
    }

    private void setupAutocomplete() {
        textFieldProfessorPersonnelNumber.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                contextMenuSuggestions.getItems().clear();

                if (newValue == null || newValue.trim().isEmpty()) {
                    contextMenuSuggestions.hide();
                } else {
                    try {
                        ArrayList<String> matches = professorDAO
                            .searchProfessorPersonalNumbers(newValue.trim());

                        if (matches.isEmpty()) {
                            contextMenuSuggestions.hide();
                        } else {
                            for (String number : matches) {
                                MenuItem item = new MenuItem(number);
                                item.setOnAction(e -> {
                                    textFieldProfessorPersonnelNumber.setText(number);
                                    contextMenuSuggestions.hide();
                                });
                                contextMenuSuggestions.getItems().add(item);
                            }
                            contextMenuSuggestions.show(
                                textFieldProfessorPersonnelNumber, Side.BOTTOM, 0, 0);
                        }

                    } catch (OperationException e) {
                        showError(e.getMessage());
                        contextMenuSuggestions.hide();
                    }
                }
            }
        );
    }

    @Override
    protected void clearFields() {
        labelFirstName.setText("");
        labelLastName.setText("");
        labelCoordinator.setText("");
        labelMessage.setText("");
        currentProfessor = null;
    }
}