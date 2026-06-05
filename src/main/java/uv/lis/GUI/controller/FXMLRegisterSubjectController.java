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
        comboBoxSection.getItems().addAll("1", "2");
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
            validateComboBox(comboBoxProfessorName.getValue(), "un profesor"),
            validateComboBox(comboBoxSection.getValue(), "una sección")
        );
        return validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    private void registerSubject() {
        Optional<Subject> subject = buildSubject();

        subject.ifPresent(newSubject -> {
            try {
                boolean isRegistered = subjectDAO.registerSubject(newSubject);
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
                        showError("No se encontró el ID para el periodo escolar seleccionado.");
                    }
            } catch (OperationException e) {
                showError("Error al consultar la base de datos: " + e.getMessage());
            } catch (NumberFormatException e) {
                showError("El formato del NRC o el ID del periodo es incorrecto.");
            }

        return validateSubject; 
    }

    @Override
    protected void clearFields() {
        textFieldNRC.clear();
        comboBoxProfessorName.getSelectionModel().clearSelection();
        comboBoxPeriodName.getSelectionModel().clearSelection();
        comboBoxSection.getSelectionModel().clearSelection();
        labelMessage.setText("");
    }
}