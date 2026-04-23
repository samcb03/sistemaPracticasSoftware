package src.test.java;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.SchoolPeriodDAO;


@ExtendWith(MockitoExtension.class)
class SchoolPeriodDAOTest {

    @Mock 
    private PreparedStatement preparedStatement;

    @Mock 
    private ResultSet resultSet;

    @Mock 
    private Connection databaseConnection;

    @Mock
    private MySQLConnectionManager connectionManager;

    private SchoolPeriodDAO schoolPeriodDAO;

    @BeforeEach
    void setUp() throws Exception {
        schoolPeriodDAO = new SchoolPeriodDAO();
        Field field = SchoolPeriodDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(schoolPeriodDAO,connectionManager);


    }

}
