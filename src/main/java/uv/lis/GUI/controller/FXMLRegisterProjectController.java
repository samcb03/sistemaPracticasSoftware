package uv.lis.GUI.controller;


import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.GUI.RegisterController;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;


public class FXMLRegisterProjectController extends RegisterController {

    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldMethodology;
    @FXML private TextField textFieldCapacity;
    @FXML private TextField textFieldObjective;
    @FXML private TextArea textAreaDescription;
    @FXML private ComboBox<String> comboBoxOrganizationName;

    private ProjectDAO projectDAO;
    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        projectDAO = new ProjectDAO();
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
        setupControls(labelError, buttonBack);
        loadOrganizationNames();
    }

    private void loadOrganizationNames() {
        try {
            ArrayList<String> organizationNames = affiliatedOrganizationDAO.getAllOrganizationNames();
            comboBoxOrganizationName.setItems(FXCollections.observableArrayList(organizationNames));
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::registerProject);
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateText(textFieldName.getText().trim(), "El nombre"),
            validateText(textFieldMethodology.getText().trim(), "La metodología"),
            validatePositiveInteger(textFieldCapacity.getText().trim(), "El cupo"),
            validateText(textFieldObjective.getText().trim(), "El objetivo"),
            validateText(textAreaDescription.getText().trim(), "La descripción")
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void registerProject() {
        Project project = buildProject();
        try {
            boolean projectRegistered = projectDAO.registerProject(project);
            if (projectRegistered) {
                showSuccess("Proyecto registrado correctamente");
                clearFields();
            } else {
                showError("Error al registrar el proyecto");
            }
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private Project buildProject() {
        Project project = new Project();
        project.setName(textFieldName.getText().trim());
        project.setMethodology(textFieldMethodology.getText().trim());
        project.setCapacity(Integer.parseInt(textFieldCapacity.getText().trim()));
        project.setObjective(textFieldObjective.getText().trim());
        project.setDescription(textAreaDescription.getText().trim());
        return project;
    }

    @Override
    public void clearFields() {
        textFieldName.clear();
        textFieldMethodology.clear();
        textFieldCapacity.clear();
        textFieldObjective.clear();
        textAreaDescription.clear();
    }
}