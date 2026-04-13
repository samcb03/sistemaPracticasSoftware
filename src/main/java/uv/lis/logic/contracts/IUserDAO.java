package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.User;


public interface IUserDAO {
    int registerUser(User user) throws SQLException;

    User authenticate(String identification, String password);
}