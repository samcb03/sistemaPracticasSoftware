package uv.lis.GUI;

import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateEndDate;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validatePhoneNumber;
import static uv.lis.logic.utils.InputValidator.validateStartDate;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.AffiliatedOrganization;

public class FormValidator {

    public static Optional<String> validateActivityForm(Activity activity) {
        String activityName = activity.getName();
        String description = activity.getDescription();
        LocalDate startDate = activity.getStartDate();
        LocalDate finalDate = activity.getEndDate();
        int hours = activity.getHoursReported();

        Stream<Optional<String>> validationStream = Stream.of(
            validateText(activityName, "El nombre de la actividad"),
            validateText(description, "La descripción"),
            validateStartDate(startDate, "La fecha de inicio"),
            validateEndDate(startDate, finalDate, "La fecha de finalización"),
            validatePositiveInteger(String.valueOf(hours), "Las horas")
        );
        
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
            
        return firstError;
    }

    public static Optional<String> validateOrganizationForm(AffiliatedOrganization organization) {
        return Stream.of(
            validateText(organization.getName(), "El nombre"),
            validateText(organization.getCity(), "La ciudad"),
            validateText(organization.getState(), "El estado"),
            validateText(organization.getSector(), "El sector"),
            validateEmail(organization.getEmail(), "El correo electrónico"),
            validatePhoneNumber(organization.getPhoneNumber(), "El número de teléfono")
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }
}