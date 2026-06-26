package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validateEmail;
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
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;

public class FXMLManageProjectSupervisorController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLManageProjectSupervisorController.class.getName());
    private static final String LABEL_INACTIVE = "Inactivo";
    private static final String LABEL_ACTIVE = "Activo";

    @FXML private Button buttonBack;
    @FXML private Button buttonSearch;
    @FXML private Button buttonUpdate;
    @FXML private Button buttonInactive;
    @FXML private Button buttonSave;
    @FXML private Label labelMessage;
    @FXML private Label labelStatus;
    @FXML private Label labelName;
    @FXML private Label labelPosition;
    @FXML private Label labelEmail;
    @FXML private Label labelOrganization;
    @FXML private TextField textFieldNameProjectSupervisor;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldPosition;
    @FXML private TextField textFieldEmail;
    @FXML private GridPane  gridPaneProjectSupervisorInfo;
    @FXML private ListView<String> listViewProjects;

    private ContextMenu contextMenuSuggestions;
    private ProjectSupervisorDAO projectSupervisorDAO;
    private ProjectSupervisor currentSupervisor;
    private boolean isEditing = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        projectSupervisorDAO = new ProjectSupervisorDAO();
        contextMenuSuggestions = new ContextMenu();
        gridPaneProjectSupervisorInfo.setVisible(false);
        buttonUpdate.setDisable(true);
        setNodeVisibility(buttonSave, false);
        setupAutocomplete();
        toggleEditMode(false);
    }

    @Override
    protected void clearFields() {
        labelName.setText("-");
        labelPosition.setText("-");
        labelEmail.setText("-");
        labelOrganization.setText("-");
        labelStatus.setText("-");
        labelMessage.setText("");
        gridPaneProjectSupervisorInfo.setVisible(false);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
        listViewProjects.getItems().clear();
    }

    @FXML
    private void searchProjectSupervisor() {
        clearFields();
        String name = textFieldNameProjectSupervisor.getText().trim();
        Optional<String> validationError = validateText(name, "Nombre del responsable");

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            executeSupervisorSearch(name);
        }
    }

    private void executeSupervisorSearch(String name) {
        try {
            Optional<ProjectSupervisor> result = projectSupervisorDAO.getProjectSupervisorByName(name);

            if (result.isPresent()) {
                currentSupervisor = result.get();
                displaySupervisorInformation(currentSupervisor);
                loadSupervisorRelatedData(name);
                gridPaneProjectSupervisorInfo.setVisible(true);
                buttonUpdate.setDisable(false);
                labelMessage.setText("");
            } else {
                showError("No se encontró ningún responsable con ese nombre");
                resetSupervisorView();
            }
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE, "Error al buscar supervisor", operationException);
            showError(operationException.getMessage());
            resetSupervisorView();
        }
    }

    private void displaySupervisorInformation(ProjectSupervisor supervisor) {
        labelName.setText(supervisor.getName());
        labelPosition.setText(supervisor.getPosition());
        labelEmail.setText(supervisor.getEmail());
        labelOrganization.setText(supervisor.getAffiliatedOrganizationName());
    }

    private void loadSupervisorRelatedData(String supervisorName) throws OperationException {
        boolean isInactive = projectSupervisorDAO.isSupervisorInactive(supervisorName);
        labelStatus.setText(isInactive ? LABEL_INACTIVE : LABEL_ACTIVE);
        buttonInactive.setDisable(isInactive);
        ArrayList<String> projects = projectSupervisorDAO.getProjectsBySupervisorName(supervisorName);
        ObservableList<String> items = FXCollections.observableArrayList(projects);
        listViewProjects.setItems(items);
    }

    @FXML
    private void saveSupervisor() {
        Optional<String> validationError = validateInputs();

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            executeSupervisorUpdate();
        }
    }

    private void resetSupervisorView() {
        gridPaneProjectSupervisorInfo.setVisible(false);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
        currentSupervisor = null;
    }

    @FXML
    private void enableEditMode() {
        if (currentSupervisor == null) {
            showError("Primero debe buscar a un Supervisor");
        } else {
            loadCurrentDataIntoEditors();
            toggleEditMode(true);
        }
    }

    private void loadCurrentDataIntoEditors() {
        textFieldName.setText(currentSupervisor.getName());
        textFieldPosition.setText(currentSupervisor.getPosition());
        textFieldEmail.setText(currentSupervisor.getEmail());
        textFieldNameProjectSupervisor.setText(currentSupervisor.getName());
    }

    @FXML
    private void handleModifyToggle() {
        if (!isEditing) {
            toggleEditMode(true);
        } else {
            Optional<String> validationError = validateInputs();

            if (validationError.isPresent()) {
                showError(validationError.get());
            } else {
                executeSupervisorUpdate();
            }
        }
    }

    private void executeSupervisorUpdate() {
        try {
            ProjectSupervisor updated = buildUpdatedSupervisor();
            boolean isUpdated = projectSupervisorDAO.modifyProjectSupervisor(updated);
            handleUpdateResult(isUpdated, updated);
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar responsable", e);
            showError(e.getMessage());
        }
    }

    private Optional<String> validateInputs() {
        Optional<String> nameValidation = Stream.of(
            validateText(textFieldName.getText(),     "El nombre"),
            validateText(textFieldPosition.getText(), "El puesto"),
            validateEmail(textFieldEmail.getText(),   "El correo"))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return nameValidation;
    }

    private ProjectSupervisor buildUpdatedSupervisor() {
        ProjectSupervisor updated = new ProjectSupervisor();
        updated.setId(currentSupervisor.getId());
        updated.setName(textFieldName.getText().trim());
        updated.setPosition(textFieldPosition.getText().trim());
        updated.setEmail(textFieldEmail.getText().trim());
        updated.setOrganizationInt(currentSupervisor.getOrganizationInt());
        updated.setAffiliatedOrganizationName(currentSupervisor.getAffiliatedOrganizationName());
        return updated;
    }

    private void handleUpdateResult(boolean isUpdated, ProjectSupervisor updated) {
        if (!isUpdated) {
            showError("No se realizaron cambios en el responsable");
        } else {
            currentSupervisor = updated;
            displaySupervisorInformation(updated);
            toggleEditMode(false);
            showSuccess("Responsable actualizado correctamente");
            LOGGER.log(Level.INFO, "Responsable actualizado: {0}", updated.getName());
        }
    }

    @FXML 
    private void inactiveSupervisor() {
        String supervisorName = labelName.getText().trim();
        Optional<String> validateError = validateText(supervisorName, 
            "El nombre del Supervisor");

        if(validateError.isPresent()) {
            showError(validateError.get());
        } else {
            executeInactivationCheck(supervisorName);
        }
    }

    private void executeInactivationCheck(String supervisorName) {
        try {

            if (projectSupervisorDAO.isSupervisorInactive(supervisorName)) {
                showError("El Supervisor ya se encuentra inactiva");
            } else {
                confirmAndInactivate(supervisorName);
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al inactivar Supervisor", e);
            showError(e.getMessage());
        }
    }

    private void confirmAndInactivate(String supervisorName) throws OperationException {
        boolean confirmed = showConfirmation("Confirmar inactivación",
             "¿Está seguro que desea inactivar al Supervisor?"
        );

        if (!confirmed) {
            showError("Inactivación cancelada");
        } else if (projectSupervisorDAO.hasProjectsActives(supervisorName)) {
            showError("El supervisor cuenta con Proyectos activos");
        } else {
            projectSupervisorDAO.inactivateProjectSupervisor(supervisorName);
            showSuccess("Supervisor inactivado con éxito");
            updateInactivationState();
        }
    }

    private void updateInactivationState() {
        labelStatus.setText(LABEL_INACTIVE);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
    }
    
    private void setupAutocomplete() {
        textFieldNameProjectSupervisor.textProperty().addListener(
            (observable, oldValue, newValue) -> handleAutocompleteChange(newValue));
        textFieldNameProjectSupervisor.focusedProperty().addListener(
            (observable, wasFocused, isFocused) -> handleFocusChange(isFocused));
    }

    private void handleFocusChange(boolean isFocused) {
        if (isFocused) {
            handleAutocompleteChange(textFieldNameProjectSupervisor.getText());
        }
    }

    private void handleAutocompleteChange(String newValue) {
        contextMenuSuggestions.getItems().clear();
        String searchValue = newValue == null ? "" : newValue.trim();

        try {
            ArrayList<String> matches =
                projectSupervisorDAO.searchProjectSupervisorName(searchValue);

            if (matches.isEmpty()) {
                contextMenuSuggestions.hide();
            } else {
                populateSuggestions(matches);
                contextMenuSuggestions.show(textFieldNameProjectSupervisor, Side.BOTTOM, 0, 0);
            }
        } catch (OperationException e) {
            LOGGER.log(Level.WARNING, "Error en autocompletado", e);
            contextMenuSuggestions.hide();
        }
    }

    private void populateSuggestions(ArrayList<String> matches) {
        for (String name : matches) {
            MenuItem item = new MenuItem(name);
            item.setOnAction(e -> {
                textFieldNameProjectSupervisor.setText(name);
                contextMenuSuggestions.hide();
            });
            contextMenuSuggestions.getItems().add(item);
        }
    }

    private void toggleEditMode(boolean isEditing) {
        this.isEditing = isEditing;
        setNodeVisibility(labelName,!isEditing);
        setNodeVisibility(labelPosition,!isEditing);
        setNodeVisibility(labelEmail,!isEditing);
        setNodeVisibility(textFieldName,isEditing);
        setNodeVisibility(textFieldPosition,isEditing);
        setNodeVisibility(textFieldEmail,isEditing);
        setNodeVisibility(buttonUpdate, !isEditing);
        setNodeVisibility(buttonSave, isEditing);
        buttonUpdate.setDisable(isEditing); 
        buttonSave.setDisable(!isEditing); 
    }
}