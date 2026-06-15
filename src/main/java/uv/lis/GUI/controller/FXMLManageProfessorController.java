package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.PROFESSOR_ID_LENGTH;
import static uv.lis.logic.utils.InputValidator.validateComboBox;
import static uv.lis.logic.utils.InputValidator.validateExactLength;
import static uv.lis.logic.utils.InputValidator.validateText;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

public class FXMLManageProfessorController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLManageProfessorController.class.getName());

    private static final String LABEL_INACTIVE = "Inactivo";
    private static final String LABEL_ACTIVE = "Activo";
    private static final String ROLE_COORDINATOR = "Coordinador";
    private static final String ROLE_PROFESSOR = "Profesor";

    @FXML private TextField textFieldProfessorPersonnelNumber;
    @FXML private Button buttonSearch;
    @FXML private GridPane gridPaneProfessorInfo;
    @FXML private Label labelPersonnelNumber;
    @FXML private Label labelFirstName;
    @FXML private Label labelLastName;
    @FXML private Label labelCoordinator;
    @FXML private Label labelStatus;
    @FXML private Button buttonUpdate;
    @FXML private Button buttonSave;
    @FXML private Button buttonInactive;
    @FXML private Button buttonBack;
    @FXML private Label labelMessage;
    @FXML private ContextMenu contextMenuSuggestions;
    @FXML private ListView<String> listViewSubjects;
    @FXML private TextField textFieldFirstName;
    @FXML private TextField textFieldLastName;
    @FXML private ComboBox<String> comboBoxCoordinator;

    private ProfessorDAO professorDAO;
    private Professor currentProfessor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        professorDAO = new ProfessorDAO();
        contextMenuSuggestions = new ContextMenu();
        setupControls(labelMessage, buttonBack);

        gridPaneProfessorInfo.setVisible(false);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
        setNodeVisibility(buttonSave, false);

        comboBoxCoordinator.getItems().addAll(ROLE_PROFESSOR, ROLE_COORDINATOR);

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
            executeProfessorSearch(personnelNumber);
        }
    }

    private void executeProfessorSearch(String personnelNumber) {
        try {
            int userId = professorDAO.getIdUserByProfessorPersonnelNumber(personnelNumber);
            Optional<Professor> validateProfessor = professorDAO.getProfessorById(userId);

            if (validateProfessor.isPresent()) {
                currentProfessor = validateProfessor.get();
                displayProfessorInformation(currentProfessor);
                loadProfessorAcademicInformation(currentProfessor.getPersonnelNumber());
                gridPaneProfessorInfo.setVisible(true);
                buttonUpdate.setDisable(false);
                labelMessage.setText("");
            } else {
                showError("No se encontró al profesor");
                resetProfessorView();
            }
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE, "Error al consultar al profesor", operationException);
            showError(operationException.getMessage());
            resetProfessorView();
        }
    }

    private void displayProfessorInformation(Professor professor) {
        labelPersonnelNumber.setText(professor.getPersonnelNumber());
        labelFirstName.setText(professor.getFirstName());
        labelLastName.setText(professor.getLastName());
        labelCoordinator.setText(professor.getIsCoordinator() ? ROLE_COORDINATOR : ROLE_PROFESSOR);
    }

    private void loadProfessorAcademicInformation(String personnelNumber) throws OperationException {
        boolean isInactive = professorDAO.isProfessorInactive(personnelNumber);
        labelStatus.setText(isInactive ? LABEL_INACTIVE : LABEL_ACTIVE);
        buttonInactive.setDisable(isInactive);

        ArrayList<String> subjects = professorDAO.getSubjectsByProfessor(personnelNumber);
        ObservableList<String> subjectItems = FXCollections.observableArrayList(subjects);
        listViewSubjects.setItems(subjectItems);
    }

    private void resetProfessorView() {
        gridPaneProfessorInfo.setVisible(false);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
        currentProfessor = null;
    }

    @FXML
    private void enableEditMode() {
        if (currentProfessor == null) {
            showError("Primero debe buscar un profesor");
        } else {
            loadCurrentDataIntoEditors();
            toggleEditMode(true);
        }
    }

    private void loadCurrentDataIntoEditors() {
        textFieldFirstName.setText(currentProfessor.getFirstName());
        textFieldLastName.setText(currentProfessor.getLastName());
        comboBoxCoordinator.setValue(currentProfessor.getIsCoordinator() ? ROLE_COORDINATOR : ROLE_PROFESSOR);
    }

    @FXML
    private void saveProfessor() {
        Optional<String> validationError = validateInputs();

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            executeProfessorUpdate();
        }
    }
    
    private void executeProfessorUpdate() {
        try {
            Professor updatedProfessor = buildUpdatedProfessor();
            boolean isUpdated = professorDAO.modifyProfessor(updatedProfessor);
            handleUpdateResult(isUpdated, updatedProfessor);
        } catch (OperationException e) {
            LOGGER.log(Level.WARNING, "Error al actualizar al profesor: {0}", e.getMessage());
            showError(e.getMessage());
        }
    }

    private Optional<String> validateInputs() {
        return Stream.of(
            validateText(textFieldFirstName.getText(), "El nombre"),
            validateText(textFieldLastName.getText(), "Los apellidos"),
            validateComboBox(comboBoxCoordinator, "rol")
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private Professor buildUpdatedProfessor() {
        Professor professor = new Professor();
        professor.setId(currentProfessor.getId());
        professor.setPersonnelNumber(currentProfessor.getPersonnelNumber());
        professor.setFirstName(textFieldFirstName.getText().trim());
        professor.setLastName(textFieldLastName.getText().trim());
        professor.setIsCoordinator(ROLE_COORDINATOR.equals(comboBoxCoordinator.getValue()));
        return professor;
    }

    private void handleUpdateResult(boolean isUpdated, Professor updatedProfessor) {
        if (!isUpdated) {
            showError("No se realizaron cambios en el profesor");
        } else {
            currentProfessor = updatedProfessor;
            displayProfessorInformation(updatedProfessor);
            toggleEditMode(false);
            showSuccess("Profesor actualizado correctamente");
            LOGGER.log(Level.INFO, "Profesor actualizado: {0}",
                updatedProfessor.getPersonnelNumber());
        }
    }

    private void toggleEditMode(boolean isEditing) {
        setNodeVisibility(labelFirstName, !isEditing);
        setNodeVisibility(labelLastName, !isEditing);
        setNodeVisibility(labelCoordinator, !isEditing);

        setNodeVisibility(textFieldFirstName, isEditing);
        setNodeVisibility(textFieldLastName, isEditing);
        setNodeVisibility(comboBoxCoordinator, isEditing);

        setNodeVisibility(buttonUpdate, !isEditing);
        setNodeVisibility(buttonSave, isEditing);
    }

    private void setNodeVisibility(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }

    private void setupAutocomplete() {
        textFieldProfessorPersonnelNumber.textProperty().addListener(
            (observable, oldValue, newValue) -> handleAutocompleteChange(newValue));
        textFieldProfessorPersonnelNumber.focusedProperty().addListener(
            (observable, wasFocused, isFocused) -> handleFocusChange(isFocused));
    }

    private void handleFocusChange(boolean isFocused) {
        if (isFocused) {
            handleAutocompleteChange(textFieldProfessorPersonnelNumber.getText());
        }
    }

    private void handleAutocompleteChange(String newValue) {
        contextMenuSuggestions.getItems().clear();
        String searchValue = newValue == null ? "" : newValue.trim();

        try {
            ArrayList<String> matches = professorDAO.searchProfessorPersonalNumbers(searchValue);

            if (matches.isEmpty()) {
                contextMenuSuggestions.hide();
            } else {
                populateSuggestions(matches);
                contextMenuSuggestions.show(
                    textFieldProfessorPersonnelNumber, Side.BOTTOM, 0, 0);
            }
        } catch (OperationException operationException) {
            LOGGER.log(Level.WARNING, "Error al cargar sugerencias", operationException);
            showError(operationException.getMessage());
            contextMenuSuggestions.hide();
        }
    }

    private void populateSuggestions(ArrayList<String> matches) {
        for (String personnelNumber : matches) {
            MenuItem item = new MenuItem(personnelNumber);
            item.setOnAction(event -> {
                textFieldProfessorPersonnelNumber.setText(personnelNumber);
                contextMenuSuggestions.hide();
            });
            contextMenuSuggestions.getItems().add(item);
        }
    }

    @FXML
    private void inactiveProfessor() {
        String personnelNumber = labelPersonnelNumber.getText().trim();
        Optional<String> validationError = validateExactLength(
            personnelNumber, PROFESSOR_ID_LENGTH, "El número de personal");

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            executeInactivationCheck(personnelNumber);
        }
    }

    private void executeInactivationCheck(String personnelNumber) {
        try {
            if (professorDAO.isProfessorInactive(personnelNumber)) {
                showError("El profesor ya se encuentra inactivo");
            } else {
                confirmAndInactivate(personnelNumber);
            }
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE, "Error al inactivar al profesor", operationException);
            showError(operationException.getMessage());
        }
    }

    private void confirmAndInactivate(String personnelNumber) throws OperationException {
        boolean confirmed = showConfirmation(
            "Confirmar inactivación",
            "¿Está seguro que desea inactivar al profesor?"
        );

        if (!confirmed) {
            showError("Inactivación cancelada.");
        } else if (professorDAO.hasSubjectAssigned(personnelNumber)) {
            showError("El profesor tiene asignaturas a su cargo. No se puede inactivar.");
        } else {
            professorDAO.inactivateProfessor(personnelNumber);
            showSuccess("El profesor ha sido inactivado correctamente.");
            updateInactivationState();
        }
    }

    private void updateInactivationState() {
        labelStatus.setText(LABEL_INACTIVE);
        buttonInactive.setDisable(true);
    }

    @Override
    protected void clearFields() {
        labelPersonnelNumber.setText("");
        labelFirstName.setText("");
        labelLastName.setText("");
        labelCoordinator.setText("");
        labelStatus.setText("");
        labelMessage.setText("");
        listViewSubjects.getItems().clear();
    }
}