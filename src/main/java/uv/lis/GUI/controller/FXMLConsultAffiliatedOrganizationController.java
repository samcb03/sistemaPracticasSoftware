package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateExactLength;

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
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

public class FXMLConsultAffiliatedOrganizationController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLConsultAffiliatedOrganizationController.class.getName());

    private static final String LABEL_INACTIVE = "Inactivo";
    private static final String LABEL_ACTIVE   = "Activo";
    private static final int POSTAL_CODE = 5;
    private static final int DOUBLE_CLICK_COUNT = 2;
    private static final int NO_SELECTION = -1;
    private static final String CONSULT_PROJECT_VIEW = "/uv/lis/GUI/view/FXMLShowProjectDetail.fxml";

    @FXML private Button buttonBack;
    @FXML private Button buttonSearch;
    @FXML private Button buttonInactive;
    @FXML private Button buttonUpdate;
    @FXML private Button buttonSave;
    @FXML private Label labelMessage;
    @FXML private Label labelStatus;
    @FXML private Label labelName;
    @FXML private Label labelCity;
    @FXML private Label labelState;
    @FXML private Label labelStreet;
    @FXML private Label labelStreetNumber;
    @FXML private Label labelPostalCode;
    @FXML private Label labelSector;
    @FXML private Label labelEmail;
    @FXML private Label labelPhoneNumber;
    @FXML private Label labelNumberOfDirectUsers;
    @FXML private Label labelNumberOfIndirectUsers;
    @FXML private ListView<String> listViewProjects;
    @FXML private TextField textFieldOrganizationName;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldCity;
    @FXML private TextField textFieldState;
    @FXML private TextField textFieldStreet;
    @FXML private TextField textFieldStreetNumber;
    @FXML private TextField textFieldPostalCode;
    @FXML private TextField textFieldSector;
    @FXML private TextField textFieldEmail;
    @FXML private TextField textFieldPhoneNumber;
    @FXML private TextField textFieldNumberOfDirectUsers;
    @FXML private TextField textFieldNumberOfIndirectUsers;
    @FXML private GridPane gridPaneOrganizationInfo;

    private ContextMenu contextMenuSuggestions;
    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;
    private AffiliatedOrganization currentOrganization;
    private ArrayList<Project> currentProjects;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
        contextMenuSuggestions = new ContextMenu();

        setupControls(labelMessage, buttonBack);
        gridPaneOrganizationInfo.setVisible(false);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
        setNodeVisibility(buttonSave, false);
        setupAutocomplete();
        configureProjectDoubleClick();
    }

    @FXML
    private void searchAffiliatedOrganization() {
        clearFields();
        String name = textFieldOrganizationName.getText().trim();
        Optional<String> validationError = validateText(name, "La organización");

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            executeOrganizationSearch(name);
        }
    }

    private void executeOrganizationSearch(String organizationName) {
        try {
            Optional<AffiliatedOrganization> result = affiliatedOrganizationDAO.getOrganizationByName(organizationName);

            if (result.isPresent()) {
                currentOrganization = result.get();
                displayOrganizationInformation(currentOrganization);
                loadOrganizationAcademicInformation(currentOrganization.getName());
                gridPaneOrganizationInfo.setVisible(true);
                buttonUpdate.setDisable(false);
                labelMessage.setText("");
            } else {
                showError("No se encontró la organización");
                resetOrganizationView();
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar organización", e);
            showError(e.getMessage());
            resetOrganizationView();
        }
    }

    private void displayOrganizationInformation(AffiliatedOrganization organization) {
        labelName.setText(organization.getName());
        labelCity.setText(organization.getCity());
        labelState.setText(organization.getState());
        labelStreet.setText(organization.getStreet());
        labelStreetNumber.setText(organization.getStreetNumber());
        labelPostalCode.setText(organization.getPostalCode());
        labelSector.setText(organization.getSector());
        labelEmail.setText(organization.getEmail());
        labelPhoneNumber.setText(organization.getPhoneNumber());
        labelNumberOfDirectUsers.setText(String.valueOf(organization.getNumberOfDirectUsers()));
        labelNumberOfIndirectUsers.setText(String.valueOf(organization.getNumberOfIndirectUsers()));
    }

    private void loadOrganizationAcademicInformation(String organizationName) throws OperationException {
        boolean isInactive = affiliatedOrganizationDAO.isOrganizationInactive(organizationName);
        labelStatus.setText(isInactive ? LABEL_INACTIVE : LABEL_ACTIVE);
        buttonInactive.setDisable(isInactive);

        ArrayList<String> projects = affiliatedOrganizationDAO.getProjectsByOrganization(currentOrganization.getName());
        ObservableList<String> items = FXCollections.observableArrayList(projects);
        listViewProjects.setItems(items);

        currentProjects = affiliatedOrganizationDAO.getCompleteProjectsByOrganization(currentOrganization.getName());
    }

    private void resetOrganizationView() {
        gridPaneOrganizationInfo.setVisible(false);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
        currentOrganization = null;
    }

    @FXML
    private void enableEditMode() {
        if (currentOrganization == null) {
            showError("Primero debe buscar una organización");
        } else {
            loadCurrentDataIntoEditors();
            toggleEditMode(true);
        }
    }

    private void loadCurrentDataIntoEditors() {
        textFieldName.setText(currentOrganization.getName());
        textFieldCity.setText(currentOrganization.getCity());
        textFieldState.setText(currentOrganization.getState());
        textFieldStreet.setText(currentOrganization.getStreet());
        textFieldStreetNumber.setText(currentOrganization.getStreetNumber());
        textFieldPostalCode.setText(currentOrganization.getPostalCode());
        textFieldEmail.setText(currentOrganization.getEmail());
        textFieldSector.setText(currentOrganization.getSector());
        textFieldNumberOfDirectUsers.setText(String.valueOf(currentOrganization.getNumberOfDirectUsers()));
        textFieldNumberOfIndirectUsers.setText(String.valueOf(currentOrganization.getNumberOfIndirectUsers()));
    }

    @FXML
    private void saveOrganization() {
        Optional<String> validationError = validateInputs();
        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            executeOrganizationUpdate();
        }
    }

    private void executeOrganizationUpdate() {
        try {
            AffiliatedOrganization updated = buildUpdatedOrganization();
            boolean isUpdated = affiliatedOrganizationDAO.modifyOrganization(updated);
            handleUpdateResult(isUpdated, updated);
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar organización", e);
            showError(e.getMessage());
        }
    }

    private Optional<String> validateInputs() {
        return Stream.of(
            validateText(textFieldName.getText(), "El nombre"),
            validateText(textFieldCity.getText(), "La ciudad"),
            validateText(textFieldState.getText(), "El estado"),
            validateEmail(textFieldEmail.getText(), "El correo"),
            validateExactLength(textFieldPostalCode.getText(), POSTAL_CODE,"El código postal")
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private AffiliatedOrganization buildUpdatedOrganization() {
        AffiliatedOrganization updated = new AffiliatedOrganization();
        updated.setId(currentOrganization.getId());
        updated.setName(textFieldName.getText().trim());
        updated.setCity(textFieldCity.getText().trim());
        updated.setStreet(textFieldStreet.getText().trim());
        updated.setStreetNumber(textFieldStreetNumber.getText().trim());
        updated.setPostalCode(textFieldPostalCode.getText().trim());
        updated.setState(textFieldState.getText().trim());
        updated.setEmail(textFieldEmail.getText().trim());
        updated.setSector(textFieldSector.getText().trim());
        updated.setPhoneNumber(textFieldPhoneNumber.getText().trim());
        updated.setNumberOfDirectUsers(Integer.parseInt(textFieldNumberOfDirectUsers.getText().trim()));
        updated.setNumberOfIndirectUsers(Integer.parseInt(textFieldNumberOfIndirectUsers.getText().trim()));
        return updated;
    }

    private void handleUpdateResult(boolean isUpdated, AffiliatedOrganization updated) {
        if (!isUpdated) {
            showError("No se realizaron cambios en la organización");
        } else {
            currentOrganization = updated;
            displayOrganizationInformation(updated);
            toggleEditMode(false);
            showSuccess("Organización actualizada correctamente");
            LOGGER.log(Level.INFO, "Organización actualizada: {0}", updated.getId());
        }
    }

    @FXML
    private void inactiveOrganization() {
        String organizationName = labelName.getText().trim();
        Optional<String> validateError = validateText(organizationName, 
            "El nombre de la Organizacion Vinculada");
            if(validateError.isPresent()) {
                showError(validateError.get());
            } else {
                executeInactivationCheck(organizationName);
            }
    }

    private void executeInactivationCheck(String organizationName) {
        try {
            if (affiliatedOrganizationDAO.isOrganizationInactive(organizationName)) {
                showError("La organización ya se encuentra inactiva");
            } else {
                confirmAndInactivate(organizationName);
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al inactivar organización", e);
            showError(e.getMessage());
        }
    }

    private void confirmAndInactivate(String organizationName) throws OperationException {
        boolean confirmed = showConfirmation("Confirmar inactivación",
            "¿Está seguro que desea inactivar esta organización?"
        );

        if (!confirmed) {
            showError("Inactivación cancelada");
        } else if(affiliatedOrganizationDAO.hasActiveProjects(organizationName)){
            showError("La organización cuenta con Proyectos activos");
        } else {
            affiliatedOrganizationDAO.inactivateOrganization(organizationName);
            showSuccess("Organización inactivada con éxito");
            updateInactivationState();
        }
    }

    private void updateInactivationState() {
        labelStatus.setText(LABEL_INACTIVE);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
    }


    private void setupAutocomplete() {
        textFieldOrganizationName.textProperty().addListener(
            (observable, oldValue, newValue) -> handleAutocompleteChange(newValue));
    }

    private void handleAutocompleteChange(String newValue) {
        contextMenuSuggestions.getItems().clear();

        if (newValue == null || newValue.trim().isEmpty()) {
            contextMenuSuggestions.hide();
        } else {
            try {
                ArrayList<String> matches = affiliatedOrganizationDAO
                    .searchActiveOrganizationsByNamePrefix(newValue.trim());

                if (matches.isEmpty()) {
                    contextMenuSuggestions.hide();
                } else {
                    populateSuggestions(matches);
                    contextMenuSuggestions.show(textFieldOrganizationName, Side.BOTTOM, 0, 0);
                }
            } catch (OperationException e) {
                LOGGER.log(Level.WARNING, "Error en autocompletado", e);
                contextMenuSuggestions.hide();
            }
        }
    }

    private void populateSuggestions(ArrayList<String> matches) {
        for (String name : matches) {
            MenuItem item = new MenuItem(name);
            item.setOnAction(e -> {
                textFieldOrganizationName.setText(name);
                contextMenuSuggestions.hide();
            });
            contextMenuSuggestions.getItems().add(item);
        }
    }

    private void configureProjectDoubleClick() {
        listViewProjects.setOnMouseClicked(this::handleProjectRowClicked);
    }

    private void handleProjectRowClicked(MouseEvent mouseEvent) {
        boolean isDoubleClick = mouseEvent.getClickCount() == DOUBLE_CLICK_COUNT;
        int selectedIndex = listViewProjects.getSelectionModel().getSelectedIndex();

        if (isDoubleClick && selectedIndex >= NO_SELECTION) {
            navigateToProjectDetail(currentProjects.get(selectedIndex));
        }
    }

    private void navigateToProjectDetail(Project project) {
        FXMLLoader loader = navigateToWithLoader(CONSULT_PROJECT_VIEW);
        if (loader != null) {
            FXMLShowProjectDetailController controller = loader.getController();
            controller.initializeData(project);
        }
    }

    private void toggleEditMode(boolean isEditing) {
        setNodeVisibility(labelName,  !isEditing);
        setNodeVisibility(labelCity,  !isEditing);
        setNodeVisibility(labelState, !isEditing);
        setNodeVisibility(labelStreet, !isEditing);
        setNodeVisibility(labelStreetNumber, !isEditing);
        setNodeVisibility(labelPostalCode, !isEditing);
        setNodeVisibility(labelEmail, !isEditing);
        setNodeVisibility(labelSector,!isEditing);
        setNodeVisibility(labelPhoneNumber,!isEditing);
        setNodeVisibility(labelNumberOfDirectUsers, !isEditing);
        setNodeVisibility(labelNumberOfIndirectUsers, !isEditing);

        setNodeVisibility(textFieldName,  isEditing);
        setNodeVisibility(textFieldCity,  isEditing);
        setNodeVisibility(textFieldState, isEditing);
        setNodeVisibility(textFieldStreet, isEditing);
        setNodeVisibility(textFieldStreetNumber, isEditing);
        setNodeVisibility(textFieldPostalCode, isEditing);
        setNodeVisibility(textFieldEmail, isEditing);
        setNodeVisibility(textFieldSector,  isEditing);
        setNodeVisibility(textFieldPhoneNumber,  isEditing);
        setNodeVisibility(textFieldNumberOfDirectUsers, isEditing);
        setNodeVisibility(textFieldNumberOfIndirectUsers, isEditing);

        setNodeVisibility(buttonUpdate, !isEditing);
        setNodeVisibility(buttonSave,    isEditing);
    }

    private void setNodeVisibility(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }

    @Override
    protected void clearFields() {
        labelName.setText("-");
        labelCity.setText("-");
        labelStreet.setText("-");
        labelStreetNumber.setText("-");
        labelPostalCode.setText("-");
        labelState.setText("-");
        labelSector.setText("-");
        labelEmail.setText("-");
        labelPhoneNumber.setText("-");
        labelNumberOfDirectUsers.setText("-");
        labelNumberOfIndirectUsers.setText("-");
        labelStatus.setText("-");
        labelMessage.setText("");
        listViewProjects.getItems().clear();
        gridPaneOrganizationInfo.setVisible(false);
        buttonInactive.setDisable(true);
        buttonUpdate.setDisable(true);
    }
}