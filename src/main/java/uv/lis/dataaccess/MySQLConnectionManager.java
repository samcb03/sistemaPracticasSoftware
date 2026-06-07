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
    private static final Logger LOGGER = Logger.getLogger(MySQLConnectionManager.class.getName());
    private Properties properties = new Properties();

    public MySQLConnectionManager() {
        chargeProperties();
    }

    private void chargeProperties() {
        try (FileInputStream propertiesFile = new FileInputStream("databaseConfig/database.properties")) {
            properties.load(propertiesFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar propiedades", e);
        }
    }

    public Connection getConnection() throws SQLException {
        String url = properties.getProperty("database.url");
        String user = properties.getProperty("database.user");
        String password = properties.getProperty("database.password");

        return DriverManager.getConnection(url, user, password);
    }
}

