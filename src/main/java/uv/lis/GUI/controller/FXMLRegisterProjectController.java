package uv.lis.GUI.controller;

import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.dao.ProjectDAO;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class FXMLRegisterProjectController implements Initializable {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_METHODOLOGY_LENGTH = 100;
    private static final int MAX_OBJECTIVE_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MIN_CAPACITY = 1;

    @FXML private Button buttonBack;
    @FXML private Button buttonRegister;
    @FXML private Pane panelForm;
    @FXML private Label labelTitle;
    @FXML private Label labelError;
    @FXML private TextField txtName;
    @FXML private TextField txtMethodology;
    @FXML private TextField txtCapacity;
    @FXML private TextField txtObjective;
    @FXML private TextArea txtDescription;

    private ProjectDAO projectDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        projectDAO = new ProjectDAO();
    }

    @FXML
    public void validateFields() {
        Optional<String> error = getFirstValidationError();
        if (error.isPresent()) {
            showError(error.get());
        } else {
            labelError.setText("");
            registerProject();
        }
    }

    private Optional<String> getFirstValidationError() {
        return Stream.of(
            validateName(txtName.getText().trim()),
            validateMethodology(txtMethodology.getText().trim()),
            validateCapacity(txtCapacity.getText().trim()),
            validateObjective(txtObjective.getText().trim()),
            validateDescription(txtDescription.getText().trim())
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private Optional<String> validateName(String name) {
        Optional<String> error;
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
            error = Optional.of("El nombre no puede estar vacío o tener más de "
                    + MAX_NAME_LENGTH + " caracteres");
        } else {
            error = Optional.empty();
        }
        return error;
    }

    private Optional<String> validateMethodology(String methodology) {
        Optional<String> error;
        if (methodology.isEmpty() || methodology.length() > MAX_METHODOLOGY_LENGTH) {
            error = Optional.of("La metodología no puede estar vacía o tener más de "
                    + MAX_METHODOLOGY_LENGTH + " caracteres");
        } else {
            error = Optional.empty();
        }
        return error;
    }

    private Optional<String> validateCapacity(String capacityStr) {
        Optional<String> error;
        if (capacityStr.isEmpty()) {
            error = Optional.of("El cupo no puede estar vacío");
        } else {
            error = validateCapacityValue(capacityStr);
        }
        return error;
    }

    private Optional<String> validateCapacityValue(String capacityStr) {
        Optional<String> error;
        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity < MIN_CAPACITY) {
                error = Optional.of("El cupo debe ser un número mayor a 0");
            } else {
                error = Optional.empty();
            }
        } catch (NumberFormatException e) {
            error = Optional.of("El cupo debe ser un número válido");
        }
        return error;
    }

    private Optional<String> validateObjective(String objective) {
        Optional<String> error;
        if (objective.isEmpty() || objective.length() > MAX_OBJECTIVE_LENGTH) {
            error = Optional.of("El objetivo no puede estar vacío o tener más de "
                    + MAX_OBJECTIVE_LENGTH + " caracteres");
        } else {
            error = Optional.empty();
        }
        return error;
    }

    private Optional<String> validateDescription(String description) {
        Optional<String> error;
        if (description.isEmpty() || description.length() > MAX_DESCRIPTION_LENGTH) {
            error = Optional.of("La descripción no puede estar vacía o tener más de "
                    + MAX_DESCRIPTION_LENGTH + " caracteres");
        } else {
            error = Optional.empty();
        }
        return error;
    }

    private void registerProject() {
        Project project = buildProject();
        try {
            boolean registered = projectDAO.registerProject(project);
            handleRegistrationResult(registered);
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void handleRegistrationResult(boolean registered) {
        if (registered) {
            showSuccess("Proyecto registrado correctamente");
            clearFields();
        } else {
            showError("Error al registrar el proyecto");
        }
    }

    private Project buildProject() {
        Project project = new Project();
        project.setName(txtName.getText().trim());
        project.setMethodology(txtMethodology.getText().trim());
        project.setCapacity(Integer.parseInt(txtCapacity.getText().trim()));
        project.setObjective(txtObjective.getText().trim());
        project.setDescription(txtDescription.getText().trim());
        return project;
    }

    private void clearFields() {
        txtName.clear();
        txtMethodology.clear();
        txtCapacity.clear();
        txtObjective.clear();
        txtDescription.clear();
    }

    private void showError(String message) {
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