package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:attendance.db";

    public static Connection connect() {

        try {
            return DriverManager.getConnection(URL);

        } catch (Exception e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
            return null;
        }
    }

    public static void initializeDatabase() {

        try {
            Connection conn = connect();

            Statement stmt = conn.createStatement();

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS lectures (" +
                            "lecture_no INTEGER PRIMARY KEY," +
                            "title TEXT NOT NULL" +
                            ");"
            );

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS questions (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "lecture_no INTEGER," +
                            "question TEXT," +
                            "option1 TEXT," +
                            "option2 TEXT," +
                            "option3 TEXT," +
                            "option4 TEXT," +
                            "correct_option INTEGER," +
                            "FOREIGN KEY(lecture_no) REFERENCES lectures(lecture_no)" +
                            ");"
            );

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS attendance_history (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "student_name TEXT," +
                            "lecture_no INTEGER," +
                            "date TEXT," +
                            "correct_answers INTEGER," +
                            "wrong_answers INTEGER," +
                            "attendance_status TEXT" +
                            ");"
            );

            conn.close();

            System.out.println("Database initialized successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}