package uv.lis.GUI.controller;


import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
import static uv.lis.logic.utils.InputValidator.validateComboBox;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dao.SchoolPeriodDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;


public class FXMLRegisterSubjectController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLRegisterSubjectController.class.getName());

    @FXML private TextField textFieldNRC;
    @FXML private ComboBox<String> comboBoxProfessorName;
    @FXML private ComboBox<String> comboBoxPeriodName;
    @FXML private Label labelSubject;
    @FXML private Label labelCareer;
    @FXML private Label labelMessage;
    @FXML private Button buttonRegister;
    @FXML private Button buttonBack;

    private SubjectDAO subjectDAO;
    private ProfessorDAO professorDAO;
    private SchoolPeriodDAO schoolPeriodDAO;
    private LinkedHashMap<String, String> professorsMap = new LinkedHashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subjectDAO = new SubjectDAO();
        professorDAO = new ProfessorDAO();
        schoolPeriodDAO = new SchoolPeriodDAO();

        setupControls(labelMessage, buttonBack);
        loadStaticLabels();
        loadProfessorsNames();
        loadPeriodsNames();
    }

    private void loadStaticLabels() {
        labelSubject.setText(Subject.getSubjectName());
        labelCareer.setText(Subject.getCareer());
    }

    private void loadProfessorsNames() {
        try {
            professorsMap = professorDAO.getAllActiveProfessorsMap();
            comboBoxProfessorName.setItems(FXCollections.observableArrayList(professorsMap.keySet()));
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void loadPeriodsNames() {
        try {
            ArrayList<String> periodsNames = schoolPeriodDAO.getAllSchoolPeriodsNames();
            comboBoxPeriodName.setItems(FXCollections.observableArrayList(periodsNames));
        } catch (OperationException e) {
            showError(e.getMessage());   
        }
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::registerSubject);
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validatePositiveInteger(textFieldNRC.getText().trim(), "El NRC"),
            validateComboBox(comboBoxPeriodName.getValue(), "un periodo escolar"),
            validateComboBox(comboBoxProfessorName.getValue(), "un profesor")
        );
        return validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    private void registerSubject() {
        Optional<Subject> subject = buildSubject();

        subject.ifPresent(s -> {
            try {
                boolean isRegistered = subjectDAO.registerSubject(s);
                if (isRegistered) {
                    showSuccess("Experiencia Educativa registrada con éxito.");
                    clearFields();
                }
            } catch (OperationException e) {
                LOGGER.log(Level.SEVERE, "Error al registrar la Experiencia Educativa", e);
                showError(e.getMessage());
            }
        });
    }

    private Optional<Subject> buildSubject() {
        Subject subject = new Subject();
        subject.setNrc(Integer.parseInt(textFieldNRC.getText().trim()));

        try {
            String personnelNumber = professorsMap.get(comboBoxProfessorName.getValue());
            subject.setProfessorPersonnelNumber(personnelNumber);

            String selectedPeriod = comboBoxPeriodName.getValue();
            String schoolPeriodId = schoolPeriodDAO.getSchoolPeriodIdByName(selectedPeriod);
            subject.setSchoolPeriodId(Integer.parseInt(schoolPeriodId));
        } catch (OperationException e) {
            showError(e.getMessage());
            return Optional.empty();
        }

        return Optional.of(subject);
    }

    @Override
    protected void clearFields() {
        textFieldNRC.clear();
        comboBoxProfessorName.getSelectionModel().clearSelection();
        comboBoxPeriodName.getSelectionModel().clearSelection();
        labelMessage.setText("");
    }
}