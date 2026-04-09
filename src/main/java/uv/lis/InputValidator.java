package uv.lis;


import java.sql.Date;
import java.util.Scanner;
import java.util.regex.Pattern;


public class InputValidator {
    private static final Pattern REPEATED_CHARS = Pattern.compile("(.)\\1{3,}");

    public static String readText(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("  El campo no puede estar vacío. Intente de nuevo.");
                continue;
            }
            if (REPEATED_CHARS.matcher(input).find()) {
                System.out.println("  El valor contiene caracteres repetidos consecutivos (ej: \"aaaa\"). Intente de nuevo.");
                continue;
            }
            return input;
        }
    }

    public static String readId(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("  El campo no puede estar vacío. Intente de nuevo.");
                continue;
            }
            return input;
        }
    }

    public static String readEmail(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("  El correo no puede estar vacío. Intente de nuevo.");
                continue;
            }
            if (!input.contains("@") || !input.contains(".")) {
                System.out.println("  El correo no tiene un formato válido (debe contener '@' y '.'). Intente de nuevo.");
                continue;
            }
            return input;
        }
    }

    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();

            if (raw.isEmpty()) {
                System.out.println("  El campo no puede estar vacío. Intente de nuevo.");
                continue;
            }
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("  El campo debe contener un número entero. Intente de nuevo.");
            }
        }
    }

    public static int readIntInRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            int value = readInt(scanner, prompt);
            if (value < min || value > max) {
                System.out.println("  El valor debe estar entre " + min + " y " + max + ". Intente de nuevo.");
            } else {
                return value;
            }
        }
    }

    public static int readMenuOption(Scanner scanner, int max) {
        return readIntInRange(scanner, "Opción: ", 1, max);
    }

    public static boolean readBoolean(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (si/no): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("si") || input.equals("sí")) {
                return true;
            } else if (input.equals("no")) {
                return false;
            } else {
                System.out.println("  Respuesta no válida. Escriba 'si' o 'no'.");
            }
        }
    }

    public static Date readDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("  La fecha no puede estar vacía. Intente de nuevo.");
                continue;
            }
            try {
                return Date.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Formato de fecha inválido. Use YYYY-MM-DD (ej: 2000-05-20).");
            }
        }
    }
}
