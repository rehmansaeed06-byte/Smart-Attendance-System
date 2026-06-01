package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class LectureDAO {

    public static void addLecture(int lectureNo, String title) {

        try {
            Connection conn = DatabaseManager.connect();

            String sql =
                    "INSERT OR IGNORE INTO lectures " +
                            "(lecture_no, title) VALUES (?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, lectureNo);
            stmt.setString(2, title);

            stmt.executeUpdate();

            conn.close();

            System.out.println("Lecture added successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getAllLectures() {

        ArrayList<String> lectures = new ArrayList<>();

        try {
            Connection conn = DatabaseManager.connect();

            String sql =
                    "SELECT * FROM lectures ORDER BY lecture_no";

            PreparedStatement stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                int lectureNo = rs.getInt("lecture_no");
                String title = rs.getString("title");

                lectures.add(
                        "Lecture " + lectureNo + " - " + title
                );
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lectures;
    }

    public static int extractLectureNo(String lectureText) {

        try {
            // Example: "Lecture 16 - Inheritance"
            String[] parts = lectureText.split(" ");

            return Integer.parseInt(parts[1]);

        } catch (Exception e) {
            return -1;
        }
    }
}