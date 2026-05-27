package uv.lis.GUI.controller;

import java.util.logging.Logger;
import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateText;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;

public class FXMLConsultProjectSupervisorController extends ValidationHandler{
@FXML private Button buttonBack;
    @FXML private Button buttonModify;
    @FXML private Button buttonSearch;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldPosition;
    @FXML private TextField textFieldEmail;
    @FXML private TextField textFieldNameProjectSupervisor;
    @FXML private Label labelName;
    @FXML private Label labelPosition;
    @FXML private Label labelEmail;
    @FXML private Label labelMessage;
    @FXML private Label labelStatus;
    @FXML private Label labelOrganization;
    @FXML private Label labelProject;
    @FXML private GridPane gridPaneProjectSupervisorInfo;
    @FXML private StackPane stackPaneName;
    @FXML private StackPane stackPanePosition;
    @FXML private StackPane stackEPanemail;
    
    private ContextMenu contextMenuSuggestions;
    private ProjectDAO projectDAO;
    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;
    private ProjectSupervisorDAO projectSupervisorDAO;
    private ProjectSupervisor projectSupervisor;
    private boolean isEditing = false;

    private static final Logger LOGGER = Logger.getLogger(
        FXMLConsultProjectSupervisorController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        projectDAO = new ProjectDAO();
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
        projectSupervisorDAO = new ProjectSupervisorDAO();
        projectSupervisor  = new ProjectSupervisor(); 
        contextMenuSuggestions = new ContextMenu(); 
        gridPaneProjectSupervisorInfo.setVisible(false); 
        buttonModify.setDisable(true);    
        buttonBack.setDisable(false);
        setupControls(labelMessage, buttonBack);         
        setupAutocomplete();
        toggleEditingMode(false);
    }

    @FXML
    private void searchProjectSupervisor() {
        clearFields();
        String supervisorName = textFieldNameProjectSupervisor.getText().trim();
        Optional<String> validateError = validateText(supervisorName, "Nombre del responsable");

        if (validateError.isPresent()) {
            showError(validateError.get());
        } else {
            executeSupervisorSearch(supervisorName);
        }
    }

    private void executeSupervisorSearch(String supervisorName) {
        try {
            Optional<ProjectSupervisor> supervisorOpt = projectSupervisorDAO.getProjectSupervisorByName(supervisorName);
            if (supervisorOpt.isPresent()) {
                projectSupervisor = supervisorOpt.get();
                displaySupervisorInformation(supervisorName);
            } else {
                showError("No se encontró ningún responsable con ese nombre.");
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar supervisor", e);
            showError(e.getMessage());
        }
    }

    private void displaySupervisorInformation(String name) {
        try {
            labelName.setText(projectSupervisor.getName());
            labelPosition.setText(projectSupervisor.getPosition());
            labelEmail.setText(projectSupervisor.getEmail());
            labelOrganization.setText(affiliatedOrganizationDAO.getOrganizationBySupervisorName(name)
                .orElse("Sin organización"));
            labelProject.setText(projectDAO.getProjectBySupervisorName(name)
                .orElse("Sin proyecto"));

            gridPaneProjectSupervisorInfo.setVisible(true);
            buttonModify.setDisable(false);
            labelMessage.setText("");
            
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar información adicional del supervisor", e);
            showError("Error al cargar datos relacionados: " + e.getMessage());
        }
    }

    @FXML
    private void handleModifyToggle() {
        if (!isEditing) {
            toggleEditingMode(true);
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
            ProjectSupervisor updatedSupervisor = buildUpdatedSupervisor();
            boolean isUpdated = projectSupervisorDAO.modifyProjectSupervisor(updatedSupervisor);
            handleUpdateResult(isUpdated, updatedSupervisor);
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar al supervisor", e);
            showError(e.getMessage());
        }
    }

    private Optional<String> validateInputs() {
        return Stream.of(
            validateText(textFieldName.getText().trim(), "El nombre"),
            validateText(textFieldPosition.getText().trim(), "El puesto"),
            validateEmail(textFieldEmail.getText().trim(), "El email")
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private ProjectSupervisor buildUpdatedSupervisor() {
        projectSupervisor.setName(textFieldName.getText().trim());
        projectSupervisor.setPosition(textFieldPosition.getText().trim());
        projectSupervisor.setEmail(textFieldEmail.getText().trim());
        return projectSupervisor;
    }

    private void handleUpdateResult(boolean isUpdated, ProjectSupervisor updated) {
        if (!isUpdated) {
            showError("No se pudieron guardar los cambios");
        } else {
            showSuccess("Modificación exitosa");
            labelName.setText(updated.getName());
            labelPosition.setText(updated.getPosition());
            labelEmail.setText(updated.getEmail());
            toggleEditingMode(false);
        }
    }

    private void toggleEditingMode(boolean editing) {
        isEditing = editing;
        buttonModify.setText(editing ? "Guardar datos" : "Modificar");
        
        labelName.setVisible(!editing); labelName.setManaged(!editing);
        textFieldName.setVisible(editing); textFieldName.setManaged(editing);
        labelPosition.setVisible(!editing); labelPosition.setManaged(!editing);
        textFieldPosition.setVisible(editing); textFieldPosition.setManaged(editing);
        labelEmail.setVisible(!editing); labelEmail.setManaged(!editing);
        textFieldEmail.setVisible(editing); textFieldEmail.setManaged(editing);

        if (editing) {
            textFieldName.setText(labelName.getText());
            textFieldPosition.setText(labelPosition.getText());
            textFieldEmail.setText(labelEmail.getText());
        }
    }

    private void setupAutocomplete() {
        textFieldNameProjectSupervisor.textProperty().addListener(
            (observable, oldValue, newValue) -> handleAutocompleteChange(newValue));
    }

    private void handleAutocompleteChange(String newValue) {
        contextMenuSuggestions.getItems().clear();
        if (newValue == null || newValue.trim().isEmpty()) {
            contextMenuSuggestions.hide();
        } else {
            try {
                ArrayList<String> matches = projectSupervisorDAO.searchProjectSupervisorName(newValue.trim());
                if (!matches.isEmpty()) {
                    populateSuggestions(matches);
                    contextMenuSuggestions.show(textFieldNameProjectSupervisor, Side.BOTTOM, 0, 0);
                } else {
                    contextMenuSuggestions.hide();
                }
            } catch (OperationException e) {
                LOGGER.log(Level.WARNING, "Error al cargar sugerencias", e);
                contextMenuSuggestions.hide();
            }
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

    @Override
    protected void clearFields() {
        gridPaneProjectSupervisorInfo.setVisible(false);
        buttonModify.setDisable(true);
        toggleEditingMode(false);
    }
}

