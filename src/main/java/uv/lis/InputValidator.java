package uv.lis;

import javafx.scene.control.TextInputControl;
import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern REPEATED_CHARS = Pattern.compile("(.)\\1{3,}");
    private static final Pattern ONLY_LETTERS = Pattern.compile("[\\p{L}\\s]+");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{7,15}$");

    public static String validateName(TextInputControl input, String fieldName, int maxLength) {
        String text = (input.getText() == null) ? "" : input.getText().trim();

        if (text.isEmpty()) {
            return fieldName + " no puede estar vacío";
        }
        if (text.length() > maxLength) {
            return fieldName + " no puede tener más de " + maxLength + " caracteres";
        }
        if (!ONLY_LETTERS.matcher(text).matches()) {
            return fieldName + " solo acepta letras";
        }
        if (REPEATED_CHARS.matcher(text).find()) {
            return fieldName + " contiene caracteres repetidos inválidos";
        }
        return null;
    }

    public static String validatePassword(TextInputControl input) {
        String text = (input.getText() == null) ? "" : input.getText();

        if (text.isEmpty()) {
            return "La contraseña no puede estar vacía";
        }
        if (text.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres";
        }
        return null;
    }

    public static String validateRequired(TextInputControl input, String fieldName) {
        String text = (input.getText() == null) ? "" : input.getText().trim();

        if (text.isEmpty()) {
            return fieldName + " es obligatorio";
        }
        return null;
    }

    public static String validateEmail(TextInputControl input) {
        String text = (input.getText() == null) ? "" : input.getText().trim();

        if (text.isEmpty()) {
            return "El correo electrónico no puede estar vacío";
        }
        if (!EMAIL_PATTERN.matcher(text).matches()) {
            return "El correo electrónico no tiene un formato válido";
        }
        return null;
    }

    public static String validatePhoneNumber(TextInputControl input) {
        String text = (input.getText() == null) ? "" : input.getText().trim();

        if (text.isEmpty()) {
            return "El número de teléfono no puede estar vacío";
        }
        if (!PHONE_PATTERN.matcher(text).matches()) {
            return "El número de teléfono solo acepta entre 7 y 15 dígitos";
        }
        return null;
    }

    public static String validatePositiveInteger(TextInputControl input, String fieldName) {
        String text = (input.getText() == null) ? "" : input.getText().trim();

        if (text.isEmpty()) {
            return fieldName + " no puede estar vacío";
        }
        try {
            int value = Integer.parseInt(text);
            if (value < 0) {
                return fieldName + " debe ser un número positivo";
            }
        } catch (NumberFormatException e) {
            return fieldName + " debe ser un número entero válido";
        }
        return null;
    }

    public static void showAlertStyle(TextInputControl input, boolean hasError) {
        if (hasError) {
            input.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
        } else {
            input.setStyle("");
        }
    }
}