package uv.lis.GUI.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FXMLRequestProjectController {
    
    @FXML private ComboBox<String> comboBoxProjects;
    @FXML private Button buttonGoBack;
    @FXML private TextField textFieldOrganization;
    @FXML private TextField textFieldMethodology;
    @FXML private TextField textFieldCapacity;
    @FXML private TextField textFieldObjective;
    @FXML private TextArea textAreaDescription;
}
