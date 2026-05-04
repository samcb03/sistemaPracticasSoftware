package uv.lis.logic.dao;

import javax.swing.filechooser.FileNameExtensionFilter;

import uv.lis.dataaccess.MySQLConnectionManager;

public class ProjectAsignationDAO {
    private MySQLConnectionManager connectionManager;


    public ProjectAsignationDAO() {
        connectionManager = new MySQLConnectionManager();
    }

    public ProjectAsignationDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    //TODO
    public boolean assignProject(String idStudent, int idProject) {
        String insertQuery = "INSERT INTO AsignacionProyecto (idEstudiante, idProyecto) VALUES (?, ?);";
        String updateStudent = "UPDATE Estudiante SET estado = 'Asignado' WHERE matricula = ?;";
        String updateProjectStatus = "UPDATE Proyecto SET estado = 'Asignado' WHERE idProyecto = ?;";


    }






}
