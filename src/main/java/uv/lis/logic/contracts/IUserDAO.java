package uv.lis.logic.contracts;

import java.util.Optional;

import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;

public interface IUserDAO {
    int registerUser(User user) throws OperationException;

    Optional<User> authenticate(String email, String password) throws AuthenticateException;
}