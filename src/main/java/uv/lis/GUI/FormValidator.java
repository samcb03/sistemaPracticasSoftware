package uv.lis.GUI;

import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateEndDate;
import static uv.lis.logic.utils.InputValidator.validateLettersOnly;
import static uv.lis.logic.utils.InputValidator.validatePhoneNumber;
import static uv.lis.logic.utils.InputValidator.validateStartDate;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.AffiliatedOrganization;

public class FormValidator {

    public static Optional<String> validateActivityForm(Activity Activity) {
        String activityName = Activity.getName();
        String description = Activity.getDescription();
        LocalDate startDate = Activity.getStartDate();
        LocalDate finalDate = Activity.getEndDate();

        Stream<Optional<String>> validationStream = Stream.of(
            validateLettersOnly(activityName, "El nombre de la actividad"),
            validateLettersOnly(description, "La descripción"),
            validateStartDate(startDate, "La fecha de inicio"),
            validateEndDate(startDate, finalDate, "La fecha de finalización")
        );
        
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
            
        return firstError;
    }

public static Optional<String> validateOrganizationForm(AffiliatedOrganization organization) {
    return Stream.of(
        validateLettersOnly(organization.getName(), "El nombre"),
        validateLettersOnly(organization.getCity(), "La ciudad"),
        validateLettersOnly(organization.getState(), "El estado"),
        validateLettersOnly(organization.getSector(), "El sector"),
        validateEmail(organization.getEmail(), "El correo electrónico"),
        validatePhoneNumber(organization.getPhoneNumber(), "El número de teléfono")
    )
    .filter(Optional::isPresent)
    .map(Optional::get)
    .findFirst();
    }
}