package uv.lis.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterProfessorController {

    @FXML private TextField txtPersonnelNumber;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkIsCoordinator;

    private ProfessorDAO professorDAO;
    private UserDAO userDAO;

    public void initialize() {
        this.professorDAO = new ProfessorDAO();
        this.userDAO = new UserDAO();
    }

    @FXML
    private void handleRegister() {
        String personnelNumber = txtPersonnelNumber.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String password = txtPassword.getText().trim();
        boolean isCoordinator = chkIsCoordinator.isSelected();

        if (personnelNumber.isEmpty() || firstName.isEmpty() 
                || lastName.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Todos los campos son obligatorios.");
            return;
        }

        try {
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(password);
            user.setUserType(isCoordinator ? "Coordinador" : "Maestro");

            int generatedId = userDAO.registerUser(user);

            Professor professor = new Professor();
            professor.setId(generatedId);
            professor.setPersonnelNumber(personnelNumber);
            professor.setFirstName(firstName);
            professor.setLastName(lastName);
            professor.setIsCoordinator(isCoordinator);

            boolean result = professorDAO.registerProfessor(professor);

            if (result) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito",
                    "Profesor registrado exitosamente.");
                clearFields();
            }
        } catch (OperationException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        clearFields();
    }

    private void clearFields() {
        txtPersonnelNumber.clear();
        txtFirstName.clear();
        txtLastName.clear();
        txtPassword.clear();
        chkIsCoordinator.setSelected(false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
