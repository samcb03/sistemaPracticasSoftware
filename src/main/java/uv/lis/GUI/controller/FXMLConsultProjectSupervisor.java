package uv.lis.GUI.controller;


import static uv.lis.logic.utils.InputValidator.validateLettersOnly;
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
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;


public class FXMLConsultProjectSupervisor extends ValidationHandler{
    @FXML private Button buttonBack;
    @FXML private Button buttonModify;
    @FXML private Button buttonSearch;
    @FXML private Label labelName;
    @FXML private Label labelPosition;
    @FXML private Label labelEmail;
    @FXML private Label labelMessage;
    @FXML private TextField textFieldNameProjectSupervisor;
    @FXML private Label labelOrganization;
    @FXML private Label labelProject;
    @FXML private GridPane gridPaneProjectSupervisorInfo;
    private ContextMenu contextMenuSuggestions;
    private ProjectDAO projectDAO;
    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;
    private ProjectSupervisorDAO projectSupervisorDAO;
    private ProjectSupervisor projectSupervisor;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        projectDAO                = new ProjectDAO();
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
        projectSupervisorDAO      = new ProjectSupervisorDAO();
        projectSupervisor         = new ProjectSupervisor(); 
        contextMenuSuggestions    = new ContextMenu();     

        setupControls(labelMessage, buttonBack);        
        gridPaneProjectSupervisorInfo.setVisible(false);  
        buttonBack.setDisable(true);
        buttonModify.setDisable(true);
        setupAutocomplete();
    }

    @FXML
    private void searchProjectSupervisor() {
        clearFields();
        String supervisorName = textFieldNameProjectSupervisor.getText().trim();

        Optional<String> validateError = validateLettersOnly(supervisorName, "Nombre del responsable");

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
                    buttonBack.setDisable(false);
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

    @Override
    protected void clearFields() {
        labelName.setText("");
        labelPosition.setText("");
        labelEmail.setText("");
        labelOrganization.setText("");
        labelProject.setText("");
        gridPaneProjectSupervisorInfo.setVisible(false);
        buttonBack.setDisable(true);
        buttonModify.setDisable(true);
    }



}
