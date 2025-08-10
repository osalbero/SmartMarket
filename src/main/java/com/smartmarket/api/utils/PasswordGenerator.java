package com.smartmarket.api.utils;

/**import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase de utilidad para generar y verificar contraseñas encriptadas con BCrypt.
 * Sirve para depurar problemas de login asegurando que la contraseña en la BD está correcta.
 */
/**public class PasswordGenerator {

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        // La contraseña en texto plano que quieres usar
        String plainPassword = "password123";

        // Genera una nueva contraseña encriptada
        String encodedPassword = encoder.encode(plainPassword);

        System.out.println("----------------------------------------------------------------");
        System.out.println("Contraseña plana: " + plainPassword);
        System.out.println("Contraseña encriptada con BCrypt (Copia este valor): " + encodedPassword);
        System.out.println("----------------------------------------------------------------");
        System.out.println();
        System.out.println("--- PRUEBA DE VERIFICACIÓN ---");

        // ⬅️ COPIA AQUÍ LA CONTRASEÑA ENCRIPTADA que obtuviste en la línea de arriba
        // Y la contraseña en texto plano para verificar que el matcher funciona correctamente
        String passwordToTest = "password123";
        String encodedHashFromDatabase = encodedPassword; // O el que tengas en tu BD

        // Compara si la contraseña en texto plano coincide con el hash encriptado
        boolean isMatch = encoder.matches(passwordToTest, encodedHashFromDatabase);
        System.out.println("El texto plano '" + passwordToTest + "' " +
                           (isMatch ? "✅ COINCIDE" : "❌ NO COINCIDE") +
                           " con el hash encriptado de la base de datos.");

        System.out.println("----------------------------------------------------------------");
    }
}*/