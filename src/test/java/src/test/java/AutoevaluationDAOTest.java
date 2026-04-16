package src.test.java;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.exceptions.OperationException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AutoevaluationDAOTest {

    @Mock
    private MySQLConnectionManager connectionManager;

    @Mock
    private Connection databaseConnection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private AutoevaluationDAO autoevaluationDAO;

    @BeforeEach
    void setUp() throws Exception {
        autoevaluationDAO = new AutoevaluationDAO();
        Field field = AutoevaluationDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(autoevaluationDAO, connectionManager);
    }

    //TODO Implementación de las pruebas de AutoevaluacionDAO


}