package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validateComboBox;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;

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
    private static final String SECTION_TAKEN_ERROR = 
        "La sección seleccionada ya está asignada en este periodo escolar.";
    private static final String NRC_FIELD = "El NRC";
    private static final String SCHOOL_PERIOD_FIELD = "Un periodo escolar";
    private static final String PROFESSOR_FIELD = "Un profesor";
    private static final String SECTION_FIELD = "Una sección";
    private static final String SUCCESSFUL_SUBJECT_REGISTER_MESSAGE = "Experiencia educativa registrada correctamente";
    private static final String ERROR_SUBJECT_REGISTER_MESSAGE = "Error al registrar la experiencia educativa";
    private static final String FAIL_SEARCH_ID_SCHOOL_PERIOD = "No se encontró el ID para el periodo escolar seleccionado";
    private static final String INVALID_NRC_MESSAGE = "El formato del NRC o el ID del periodo es incorrecto";
    private static final String SECTION_1 = "1";
    private static final String SECTION_2 = "2";

    @FXML private TextField textFieldNRC;
    @FXML private ComboBox<String> comboBoxProfessorName;
    @FXML private ComboBox<String> comboBoxPeriodName;
    @FXML private ComboBox<String> comboBoxSection;
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
        loadSections();
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::performRegistration);
    }

    @Override
    protected void clearFields() {
        textFieldNRC.clear();
        comboBoxProfessorName.getSelectionModel().clearSelection();
        comboBoxPeriodName.getSelectionModel().clearSelection();
        comboBoxSection.getSelectionModel().clearSelection();
        labelMessage.setText("");
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

    private void loadSections() {
        comboBoxSection.getItems().addAll(SECTION_1, SECTION_2);
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validatePositiveInteger(textFieldNRC.getText().trim(), NRC_FIELD),
            validateComboBox(comboBoxPeriodName.getValue(), SCHOOL_PERIOD_FIELD),
            validateComboBox(comboBoxProfessorName.getValue(), PROFESSOR_FIELD),
            validateComboBox(comboBoxSection.getValue(), SECTION_FIELD)
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void performRegistration() {
        Optional<Subject> subject = buildSubject();
        subject.ifPresent(this::registerSubject);
    }

    private void registerSubject(Subject subject) {
        try {
            boolean isSectionTaken = subjectDAO.isSectionTakenInPeriod(
                subject.getSchoolPeriodId(), subject.getSection());
            if (isSectionTaken) {
                showError(SECTION_TAKEN_ERROR);
            } else {
                boolean isRegistered = subjectDAO.registerSubject(subject);
                if (isRegistered) {
                    showSuccess(SUCCESSFUL_SUBJECT_REGISTER_MESSAGE);
                    clearFields();
                }
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, ERROR_SUBJECT_REGISTER_MESSAGE, e);
            showError(e.getMessage());
        }
    }

    private Optional<Subject> buildSubject() {
        Optional<Subject> validateSubject = Optional.empty();
        try {
            Subject subject = new Subject();
            subject.setNrc(Integer.parseInt(textFieldNRC.getText().trim()));
            subject.setSection(comboBoxSection.getValue());
            String personnelNumber = professorsMap.get(comboBoxProfessorName.getValue());
            subject.setProfessorPersonnelNumber(personnelNumber);
            String selectedPeriod = comboBoxPeriodName.getValue();
            Optional<String> validatePeriodId = schoolPeriodDAO.getSchoolPeriodIdByName(selectedPeriod);

            if (validatePeriodId.isPresent()) {
                int periodId = Integer.parseInt(validatePeriodId.get());
                subject.setSchoolPeriodId(periodId);
                validateSubject = Optional.of(subject);
            } else {
                showError(FAIL_SEARCH_ID_SCHOOL_PERIOD);
            }
        } catch (OperationException e) {
            showError(e.getMessage());
        } catch (NumberFormatException e) {
            showError(INVALID_NRC_MESSAGE);
        }
        return validateSubject;
    }
}