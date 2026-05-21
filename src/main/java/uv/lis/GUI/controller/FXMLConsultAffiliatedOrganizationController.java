package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validateText;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.control.TextField;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;

public class FXMLConsultAffiliatedOrganizationController extends ValidationHandler  {

    @FXML private Button buttonBack;
    @FXML private Button buttonSearch;
    @FXML private Button buttonInactive;
    @FXML private Button buttonUpdate;
    @FXML private Label label;
    @FXML private Label labelName;
    @FXML private Label labelCity;
    @FXML private Label labelState;
    @FXML private Label labelSector;
    @FXML private Label labelEmail;
    @FXML private Label labelPhoneNumber;
    @FXML private Label labelNumberOfDirectUsers;
    @FXML private Label labelNumberOfIndirectUsers;
    @FXML private Label labelMessage;
    @FXML private Label labelStatus;
    @FXML private ListView<String> listViewProjects;
    @FXML private TextField textFieldOrganizationName;
    @FXML private GridPane gridPaneOrganizationInfo;
    @FXML private ContextMenu contextMenuSuggestions;

    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;
    private ProjectDAO projectDAO;
    private ProjectSupervisorDAO projectSupervisorDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        projectDAO = new ProjectDAO();
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
        projectSupervisorDAO = new ProjectSupervisorDAO();
        contextMenuSuggestions = new ContextMenu();
        gridPaneOrganizationInfo.setVisible(false);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
        setupControls(labelMessage, buttonBack);
        setupAutocomplete();

    }

    @FXML
    private void searchAffiliatedOrganization() {
        clearFields();
        String organizationName = textFieldOrganizationName.getText().trim();

        Optional<String> validationError = validateText(organizationName, "La organización");

        if(validationError.isPresent()) {
            showError(validationError.get());
        } else {
            try {
                int id = affiliatedOrganizationDAO.getOrganizationIdByName(organizationName);
                Optional<AffiliatedOrganization> validateOrganization = affiliatedOrganizationDAO.getOrganizationById(id);

                if(validateOrganization.isPresent()) {
                    AffiliatedOrganization affiliatedOrganization = validateOrganization.get();
                    labelName.setText(affiliatedOrganization.getName());
                    labelCity.setText(affiliatedOrganization.getCity());
                    labelState.setText(affiliatedOrganization.getState());
                    labelSector.setText(affiliatedOrganization.getSector());
                    labelEmail.setText(affiliatedOrganization.getEmail());
                    labelPhoneNumber.setText(affiliatedOrganization.getPhoneNumber());
                    labelNumberOfDirectUsers.setText(String.valueOf(affiliatedOrganization.getNumberOfDirectUsers()));
                    labelNumberOfIndirectUsers.setText(String.valueOf(affiliatedOrganization.getNumberOfIndirectUsers()));

                        boolean isInactive = affiliatedOrganizationDAO.isOrganizationInactive(id);
                        labelStatus.setText(isInactive ? "Inactivo" : "Activo");                   
                        buttonInactive.setDisable(isInactive);
                        buttonUpdate.setDisable(false);
                        gridPaneOrganizationInfo.setVisible(true);
                        labelMessage.setText("");
                        loadAssignedProjects(id);
                                        
                } else {
                    showError("No se encontró la organización");
                    gridPaneOrganizationInfo.setVisible(false);
                    buttonInactive.setDisable(true);
                    buttonUpdate.setDisable(true);
                }

            } catch (OperationException e) {
                showError("Error de base de datos: " + e.getMessage());
                gridPaneOrganizationInfo.setVisible(false);
                buttonInactive.setDisable(true);
                buttonUpdate.setDisable(true);
            }
        }
    }

    private void loadAssignedProjects(int organizationId) {
        listViewProjects.getItems().clear();
        try {
            ArrayList<String> projects = projectDAO.getProjectNamesByOrganizationId(organizationId);
            if (projects != null && !projects.isEmpty()) {
                listViewProjects.getItems().addAll(projects);
            } else {
                listViewProjects.getItems().add("No hay proyectos asignados a esta organización.");
            }
        } catch (OperationException e) {
            listViewProjects.getItems().add("Error al cargar los proyectos de la organización.");
            showError("Error de base de datos al recuperar proyectos: " + e.getMessage());
        }
    }
        

    private void setupAutocomplete() {
        textFieldOrganizationName.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                contextMenuSuggestions.getItems().clear();

                if (newValue == null || newValue.trim().isEmpty()) {
                    contextMenuSuggestions.hide();
                } else {
                    try {
                        ArrayList<String> matches = affiliatedOrganizationDAO.searchOrganizationByName(newValue.trim());

                        if (matches.isEmpty()) {
                            contextMenuSuggestions.hide();
                        } else {
                            for (String number : matches) {
                                MenuItem item = new MenuItem(number);
                                item.setOnAction(e -> {
                                    textFieldOrganizationName.setText(number);
                                    contextMenuSuggestions.hide();
                                });
                                contextMenuSuggestions.getItems().add(item);
                            }
                            contextMenuSuggestions.show(
                                textFieldOrganizationName, Side.BOTTOM, 0, 0);
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
        labelName.setText("-");
        labelCity.setText("-");
        labelState.setText("-");
        labelSector.setText("-");
        labelEmail.setText("-");
        labelPhoneNumber.setText("-");
        labelNumberOfDirectUsers.setText("-");
        labelNumberOfIndirectUsers.setText("-");
        labelStatus.setText("-");
        labelMessage.setText("");
        gridPaneOrganizationInfo.setVisible(false);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
    }

}
