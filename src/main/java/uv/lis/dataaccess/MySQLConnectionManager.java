package uv.lis.dataaccess;


/*import java.io.FileInputStream;
import java.io.IOException;*/
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/*import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;*/


public class MySQLConnectionManager {
    
    private static String databaseUrl = "jdbc:mysql://localhost:3306/PracticasSoftware"
         + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "Administrador";
    private static String password = "admin_DB@135!";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, user, password);
    } 

    public static MySQLConnectionManager getInstance() {
        return new MySQLConnectionManager();
    }
}

/*public class MySQLConnectionManager {
    private static final Logger LOGGER = Logger.getLogger(MySQLConnectionManager.class);
    
    private Properties properties = new Properties();
    
    public MySQLConnectionManager() {
        cargarPropiedades();
    }


private void cargarPropiedades() {
        try (FileInputStream archivoPropiedades = new FileInputStream("mi ruta")) {
            properties.load(archivoPropiedades);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar el archivo de propiedades", e);
        }
    }

    private Connection getConnection(String userType) {
        String url = properties.getProperty("base_de_datos.url");
        String usuario = properties.getProperty("base_de_datos." + userType + ".usuario");
        String contrasenia = properties.getProperty("base_de_datos." + userType + ".contrasenia");
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(url, usuario, contrasenia);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectarse a la base de datos como " + usuario + ": ", e);
        }
        return conexion;
    }

    public Connection getProfessorConnection() {
        return getConnection("profesor");
    }

    public Connection getStudentConnection() {
        return getConnection("estudiante");
    }

    public Connection getAdministratorConnection() {
        return getConnection("administrador");
    }

    public Connection getCoordinatorConnection() {
        return getConnection("coordinador");
    }

} */

