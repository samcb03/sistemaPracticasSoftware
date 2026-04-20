package uv.lis.logic.contracts;


import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;


public interface IUserDAO {
    int registerUser(User user) throws OperationException;

    User authenticate(String identification, String password) throws AuthenticateException;
}