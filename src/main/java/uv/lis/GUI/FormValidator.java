package uv.lis.GUI;

import java.util.Optional;
import java.util.stream.Stream;
import static uv.lis.logic.utils.InputValidator.*;

public class FormValidator {

    public static Optional<String> validateActivityForm(String activityName, String description) {
        Stream<Optional<String>> validationStream = Stream.of(
            validateLettersOnly(activityName, "El nombre de la actividad"),
            validateLettersOnly(description, "La descripción")
        );
        
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
            
        return firstError;
    }

    public static Optional<String> validateOrganizationForm(String name, String city, String state, 
                                                            String sector, String email, String phone, 
                                                            String directUsers, String indirectUsers) {
        Stream<Optional<String>> validationStream = Stream.of(
            validateLettersOnly(name, "El nombre"),
            validateLettersOnly(city, "La ciudad"),
            validateLettersOnly(state, "El estado"),
            validateLettersOnly(sector, "El sector"),
            validateEmail(email, "El correo electrónico"),
            validatePhoneNumber(phone, "El número de teléfono"),
            validatePositiveInteger(directUsers, "El número de usuarios directos"),
            validatePositiveInteger(indirectUsers, "El número de usuarios indirectos")
        );
        
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
            
        return firstError;
    }
}