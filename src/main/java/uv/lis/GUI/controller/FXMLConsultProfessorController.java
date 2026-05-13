package uv.lis.GUI.controller;


import static uv.lis.logic.utils.InputValidator.PROFESSOR_ID_LENGTH;
import static uv.lis.logic.utils.InputValidator.validateExactLength;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;


public class FXMLConsultProfessorController extends ValidationHandler {

    @FXML private TextField textFieldProfessorPersonnelNumber;
    @FXML private Button buttonSearch;
    @FXML private GridPane gridPaneProfessorInfo;
    @FXML private Label labelPersonnelNumber;
    @FXML private Label labelFirstName;
    @FXML private Label labelLastName;
    @FXML private Label labelCoordinator;
    @FXML private Label labelStatus;
    @FXML private Button buttonUpdate;
    @FXML private Button buttonInactive;
    @FXML private Button buttonBack;
    @FXML private Label labelMessage;
    @FXML private ContextMenu contextMenuSuggestions;

    private ProfessorDAO professorDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        professorDAO = new ProfessorDAO();
        contextMenuSuggestions = new ContextMenu();
        setupControls(labelMessage, buttonBack); 
        
        gridPaneProfessorInfo.setVisible(false);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
        
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
                Professor professor = professorDAO.getProfessorById(userId);

                labelPersonnelNumber.setText(professor.getPersonnelNumber());
                labelFirstName.setText(professor.getFirstName());
                labelLastName.setText(professor.getLastName());
                labelCoordinator.setText(professor.getIsCoordinator() ? "Coordinador" : "Profesor");

                boolean isInactive = professorDAO.isProfessorInactive(professor.getPersonnelNumber());
                labelStatus.setText(isInactive ? "Inactivo" : "Activo");
                
                buttonInactive.setDisable(isInactive);
                
                gridPaneProfessorInfo.setVisible(true);
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

    @FXML 
    private void inactiveProfessor() {
        String personnelNumber = labelPersonnelNumber.getText().trim();
        Optional<String> validationError = validateExactLength(
            personnelNumber, PROFESSOR_ID_LENGTH, "El número de personal");

        if(validationError.isPresent()) {
            showError(validationError.get());
        } else {
            try {
                if(professorDAO.isProfessorInactive(personnelNumber)) {
                    showError("El profesor ya se encuentra inactivo");
                } else {
                    confirmAndInactivate(personnelNumber);
                }
            } catch (OperationException e) {
                showError(e.getMessage());
            }
        }
    }

    private void confirmAndInactivate(String personnelNumber) throws OperationException {
        boolean confirmed = showConfirmation(
            "Confirmar inactivación",
            "¿Está seguro que desea inactivar al profesor?"
        );

        if (confirmed) {
            if (professorDAO.hasSubjectAssigned(personnelNumber)) {
                boolean confirmedAnyway = showConfirmation(
                    "Profesor asignado a experiencias educativas",
                    "El profesor tiene clases asignadas. ¿Desea inactivarlo y removerlo de sus clases de todas formas?"
                );
                
                if (confirmedAnyway) {
                    professorDAO.inactivateProfessor(personnelNumber);
                    
                    showSuccess("El profesor ha sido inactivado y removido de sus clases correctamente.");
                    
                    labelStatus.setText("Inactivo");
                    buttonInactive.setDisable(true);
                } else {
                    showError("Inactivación cancelada");
                }
            } else {
                professorDAO.inactivateProfessor(personnelNumber);
                
                showSuccess("El profesor ha sido inactivado correctamente.");
                
                labelStatus.setText("Inactivo");
                buttonInactive.setDisable(true);
            }
        } else {
            showError("Inactivación cancelada.");
        }
    }

    private boolean showConfirmation(String title, String message) {
        boolean confirmed = false;
        ButtonType yesButton = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Stage owner = (Stage) buttonInactive.getScene().getWindow();
        alert.initOwner(owner);

        Optional<ButtonType> result = alert.showAndWait();
        confirmed = result.isPresent() && result.get() == yesButton;
        return confirmed;
    }

    @Override
    protected void clearFields() {
        labelPersonnelNumber.setText("");
        labelFirstName.setText("");
        labelLastName.setText("");
        labelCoordinator.setText("");
        labelStatus.setText("");
        labelMessage.setText("");
    }
}