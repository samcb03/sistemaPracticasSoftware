package uv.lis.GUI.controller;


import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import static uv.lis.logic.utils.InputValidator.MAX_TEXT_LENGTH;
import static uv.lis.logic.utils.InputValidator.MIN_POSITIVE_INTEGER;


public class FXMLRegisterProjectController implements Initializable {

    @FXML private Button buttonBack;
    @FXML private Button buttonRegister;
    @FXML private Label labelError;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldMethodology;
    @FXML private TextField textFieldCapacity;
    @FXML private TextField textFieldObjective;
    @FXML private TextArea textFieldDescription;
    @FXML private ComboBox<String> comboBoxOrganizationName;

    private ProjectDAO projectDAO;
    private AffiliatedOrganization affiliatedOrganization;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        projectDAO = new ProjectDAO();

        comboBoxOrganizationName.setItems(
            FXCollections.observableArrayList(affiliatedOrganization.getAllOrganizationNames)
        );
    }

    @FXML
    public void validateFields() {
        Optional<String> message = getValidationMessage();
        if (message.isPresent()) {
            showMessage(message.get());
        } else {
            labelError.setText("");
            registerProject();
        }
    }

    private Optional<String> getValidationMessage() {
        return Stream.of(
            validateName(textFieldName.getText().trim()),
            validateMethodology(textFieldMethodology.getText().trim()),
            validateCapacity(textFieldCapacity.getText().trim()),
            validateObjective(textFieldObjective.getText().trim()),
            validateDescription(textFieldDescription.getText().trim())
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private Optional<String> validateName(String name) {
        Optional<String> message;
        if (name.isEmpty() || name.length() > MAX_TEXT_LENGTH) {
            message = Optional.of("El nombre no puede estar vacío o tener más de "
                + MAX_TEXT_LENGTH + " caracteres");
        } else {
            message = Optional.empty();
        }
        return message;
    }

    private Optional<String> validateMethodology(String methodology) {
        Optional<String> message;
        if (methodology.isEmpty() || methodology.length() > MAX_TEXT_LENGTH) {
            message = Optional.of("La metodología no puede estar vacía o tener más de "
                    + MAX_TEXT_LENGTH + " caracteres");
        } else {
            message = Optional.empty();
        }
        return message;
    }

    private Optional<String> validateCapacity(String capacityStr) {
        Optional<String> message;
        if (capacityStr.isEmpty()) {
            message = Optional.of("El cupo no puede estar vacío");
        } else {
            message = validateCapacityValue(capacityStr);
        }
        return message;
    }

    private Optional<String> validateCapacityValue(String capacityStr) {
        Optional<String> message;
        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity < MIN_POSITIVE_INTEGER) {
                message = Optional.of("El cupo debe ser un número mayor a 0");
            } else {
                message = Optional.empty();
            }
        } catch (NumberFormatException e) {
            message = Optional.of("El cupo debe ser un número válido");
        }
        return message;
    }

    private Optional<String> validateObjective(String objective) {
        Optional<String> message;
        if (objective.isEmpty() || objective.length() > MAX_TEXT_LENGTH) {
            message = Optional.of("El objetivo no puede estar vacío o tener más de "
                    + MAX_TEXT_LENGTH + " caracteres");
        } else {
            message = Optional.empty();
        }
        return message;
    }

    private Optional<String> validateDescription(String description) {
        Optional<String> message;
        if (description.isEmpty() || description.length() > MAX_TEXT_LENGTH) {
            message = Optional.of("La descripción no puede estar vacía o tener más de "
                + MAX_TEXT_LENGTH + " caracteres");
        } else {
            message = Optional.empty();
        }
        return message;
    }

    private void registerProject() {
        Project project = buildProject();
        try {
            boolean registered = projectDAO.registerProject(project);
            handleRegistrationResult(registered);
        } catch (OperationException e) {
            showMessage(e.getMessage());
        }
    }

    private void handleRegistrationResult(boolean registered) {
        if (registered) {
            showSuccess("Proyecto registrado correctamente");
            clearFields();
        } else {
            showMessage("Error al registrar el proyecto");
        }
    }

    private Project buildProject() {
        Project project = new Project();
        project.setName(textFieldName.getText().trim());
        project.setMethodology(textFieldMethodology.getText().trim());
        project.setCapacity(Integer.parseInt(textFieldCapacity.getText().trim()));
        project.setObjective(textFieldObjective.getText().trim());
        project.setDescription(textFieldDescription.getText().trim());
        return project;
    }

    private void clearFields() {
        textFieldName.clear();
        textFieldMethodology.clear();
        textFieldCapacity.clear();
        textFieldObjective.clear();
        textFieldDescription.clear();
    }

    private void showMessage(String message) {
        labelError.setText(message);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goBack() {
        Stage stage = (Stage) buttonBack.getScene().getWindow();
        stage.close();
    }
}