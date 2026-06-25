package uv.lis.dataaccess;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLConnectionManager {
    private static final String PROPERTIES_PATH = "databaseConfig/database.properties";
    private static final String PROPERTY_URL = "database.url";
    private static final String PROPERTY_USER = "database.user";
    private static final String PROPERTY_PASSWORD = "database.password";
    private static final String MESSAGE_CONNECTION_FAILED = "No se pudo establecer conexión con la base de datos.";

    private static final Logger LOGGER = Logger.getLogger(MySQLConnectionManager.class.getName());

    private Properties properties = new Properties();

    public MySQLConnectionManager() {
        chargeProperties();
    }

    public Connection getConnection() throws SQLException {
        String url = properties.getProperty(PROPERTY_URL);
        String user = properties.getProperty(PROPERTY_USER);
        String password = properties.getProperty(PROPERTY_PASSWORD);

        Connection databaseConnection;

        try {
            databaseConnection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion a la base de datos", e);
            throw new SQLException(MESSAGE_CONNECTION_FAILED, e.getSQLState(), e);
        }

        return databaseConnection;
    }

    private void chargeProperties() {
        try (FileInputStream propertiesFile = new FileInputStream(PROPERTIES_PATH)) {
            properties.load(propertiesFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar propiedades", e);
        }
    }
}