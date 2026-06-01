package database;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;

public class UserValidator {

    public static boolean validateTeacher(String username, String password) {
        return validateUser("users/teachers.txt", username, password);
    }

    public static boolean validateStudent(String username, String password) {
        return validateUser("users/students.txt", username, password);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return password; // fallback if hashing fails
        }
    }

    private static boolean validateUser(String filePath, String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String fileUsername = parts[0].trim();
                    String filePassword = parts[1].trim();
                    if (fileUsername.equalsIgnoreCase(username)) {
                        // Support both plaintext (legacy) and hashed passwords
                        boolean matchPlain = filePassword.equals(password);
                        boolean matchHashed = filePassword.equals(hashPassword(password));
                        if (matchPlain || matchHashed) return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("User validation error: " + e.getMessage());
        }
        return false;
    }

    public static boolean changeTeacherPassword(String username, String newPassword) {
        return changePassword("users/teachers.txt", username, newPassword);
    }

    public static boolean changeStudentPassword(String username, String newPassword) {
        return changePassword("users/students.txt", username, newPassword);
    }

    private static boolean changePassword(String filePath, String username, String newPassword) {
        ArrayList<String> lines = new ArrayList<>();
        boolean changed = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(username)) {
                    // Store hashed password
                    lines.add(parts[0].trim() + "," + hashPassword(newPassword));
                    changed = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (Exception e) {
            System.out.println("Password change error: " + e.getMessage());
            return false;
        }

        if (changed) {
            try (FileWriter writer = new FileWriter(filePath)) {
                for (String updatedLine : lines) {
                    writer.write(updatedLine + "\n");
                }
            } catch (Exception e) {
                System.out.println("Password write error: " + e.getMessage());
                return false;
            }
        }

        return changed;
    }
}
