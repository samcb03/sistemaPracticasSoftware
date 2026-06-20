package uv.lis.logic.exceptions;

import java.sql.SQLException;
import java.util.Map;

public class OperationException extends Exception {

    private static final int SQL_STATE_CLASS_LENGTH = 2;
    
    private static final Map<String, String> SQL_STATE_MESSAGES = Map.of(
        "08", "Error de conexion con la base de datos. Intente mas tarde",
        "28", "Acceso denegado a la base de datos.",
        "42", "Configuración o permisos incorrectos. Contacte al administrador",
        "22", "Uno de los campos excede la longitud o el formato permitido",
        "23", "Ya existe un registro con esos datos");

    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return resolveMessage(super.getMessage(), getCause());
    }

    static String resolveMessage(String defaultMessage, Throwable cause) {
        String message = defaultMessage;
        String stateClass = extractSqlStateClass(cause);
        if (stateClass != null) {
            message = SQL_STATE_MESSAGES.getOrDefault(stateClass, defaultMessage);
        }
        return message;
    }

    private static String extractSqlStateClass(Throwable cause) {
        String stateClass = null;
        if (cause instanceof SQLException) {
            String sqlState = ((SQLException) cause).getSQLState();
            if (sqlState != null && sqlState.length() >= SQL_STATE_CLASS_LENGTH) {
                stateClass = sqlState.substring(0, SQL_STATE_CLASS_LENGTH);
            }
        }
        return stateClass;
    }
}