package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateText;

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

    @FXML
    private void searchProjectSupervisor() {
        clearFields();
        String supervisorName = textFieldNameProjectSupervisor.getText().trim();

        Optional<String> validateError = validateText(supervisorName, "Nombre del responsable");

        if (validateError.isPresent()) {
            showError(validateError.get());
        } else {
            try {
                Optional<ProjectSupervisor> supervisorOpt = projectSupervisorDAO
                    .getProjectSupervisorByName(supervisorName);

                if (supervisorOpt.isPresent()) {
                    projectSupervisor = supervisorOpt.get();

                    labelName.setText(projectSupervisor.getName());
                    labelPosition.setText(projectSupervisor.getPosition());
                    labelEmail.setText(projectSupervisor.getEmail());
                    labelOrganization.setText(
                        affiliatedOrganizationDAO
                            .getOrganizationBySupervisorName(supervisorName)
                            .orElse("Sin organización")
                    );
                    labelProject.setText(
                        projectDAO
                            .getProjectBySupervisorName(supervisorName)
                            .orElse("Sin proyecto")
                    );

                    gridPaneProjectSupervisorInfo.setVisible(true);
                    buttonModify.setDisable(false);
                    labelMessage.setText("");

                } else {
                    showError("No se encontró ningún responsable con ese nombre.");
                }

            } catch (OperationException e) {
                showError(e.getMessage());
            }
        }
    }

    private void setupAutocomplete() {
        textFieldNameProjectSupervisor.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                contextMenuSuggestions.getItems().clear();

                if (newValue == null || newValue.trim().isEmpty()) {
                    contextMenuSuggestions.hide();
                } else {
                    try {
                        ArrayList<String> matches = projectSupervisorDAO
                            .searchProjectSupervisorName(newValue.trim());

                        if (matches.isEmpty()) {
                            contextMenuSuggestions.hide();
                        } else {
                            for (String number : matches) {
                                MenuItem item = new MenuItem(number);
                                item.setOnAction(e -> {
                                    textFieldNameProjectSupervisor.setText(number);
                                    contextMenuSuggestions.hide();
                                });
                                contextMenuSuggestions.getItems().add(item);
                            }
                            contextMenuSuggestions.show(
                                textFieldNameProjectSupervisor, Side.BOTTOM, 0, 0);
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
    private void handleModifyToggle() {
        if(!isEditing) {
            toggleEditingMode(true);
        } else {
            if(validateEmail(textFieldEmail.getText().trim(), "Email").isPresent()) {
                showError("Email inválido");
            } else {
                try {
                    projectSupervisor.setName(textFieldName.getText().trim());
                    projectSupervisor.setPosition(textFieldPosition.getText().trim());
                    projectSupervisor.setEmail(textFieldEmail.getText().trim());
                            
                    if (projectSupervisorDAO.modifyProjectSupervisor(projectSupervisor)) {
                        showSuccess("Modificación exitosa");
                        labelName.setText(projectSupervisor.getName());
                        labelPosition.setText(projectSupervisor.getPosition());
                        labelEmail.setText(projectSupervisor.getEmail());
                        toggleEditingMode(false);
                    } else showError("Falló la modificación");
                } catch (OperationException e) { 
                    showError(e.getMessage()); 
                }
            }
        }
    }

    @Override
    protected void clearFields() {
        gridPaneProjectSupervisorInfo.setVisible(false);
        buttonModify.setDisable(true);
        toggleEditingMode(false);
    }
}
