package uv.lis.dataaccess;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class MySQLConnectionManager {
    
    private static String databaseUrl = "jdbc:mysql://localhost:3306/PracticasSoftware"
         + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC-6";
    private static String user = "Administrador";
    private static String password = "admin_DB@135!";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, user, password);
    } 

    public static MySQLConnectionManager getInstance() {
        return new MySQLConnectionManager();
    }
}
