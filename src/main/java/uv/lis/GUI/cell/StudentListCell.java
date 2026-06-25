package uv.lis.GUI.cell;

import javafx.scene.control.ListCell;

import uv.lis.logic.dto.Student;

public class StudentListCell extends ListCell<Student> {

    @Override
    protected void updateItem(Student student, boolean isEmpty) {
        super.updateItem(student, isEmpty);

        if (isEmpty || student == null) {
            setText(null);
        } else {
            setText(student.getIdStudent() + " - "
                + student.getFirstName() + " " + student.getLastName());
        }
    }
}