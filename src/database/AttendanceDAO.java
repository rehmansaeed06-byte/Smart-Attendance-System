package database;

import models.AttendanceRecord;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class AttendanceDAO {

    public static void saveAttendance(
            String studentName,
            int lectureNo,
            int correctAnswers,
            int wrongAnswers,
            String attendanceStatus
    ) {

        try {

            Connection con = DatabaseManager.connect();

            String sql =
                    "INSERT INTO attendance_history " +
                            "(student_name, lecture_no, date, correct_answers, wrong_answers, attendance_status) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, studentName);
            ps.setInt(2, lectureNo);

            String dateTime =
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                    );

            ps.setString(3, dateTime);
            ps.setInt(4, correctAnswers);
            ps.setInt(5, wrongAnswers);
            ps.setString(6, attendanceStatus);

            ps.executeUpdate();

            con.close();

            System.out.println("Attendance saved successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<AttendanceRecord> getAttendanceHistory() {

        ArrayList<AttendanceRecord> list = new ArrayList<>();

        try {

            Connection con = DatabaseManager.connect();

            String sql =
                    "SELECT * FROM attendance_history ORDER BY id ASC";

            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                AttendanceRecord record =
                        new AttendanceRecord(
                                rs.getString("student_name"),
                                rs.getInt("lecture_no"),
                                rs.getString("date"),
                                rs.getInt("correct_answers"),
                                rs.getInt("wrong_answers"),
                                rs.getString("attendance_status")
                        );

                list.add(record);
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static String getAttendanceHistoryText() {

        ArrayList<AttendanceRecord> records =
                getAttendanceHistory();

        if (records.isEmpty()) {
            return "No attendance history found.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Attendance History\n");
        sb.append("----------------------------\n");

        for (AttendanceRecord record : records) {

            sb.append("Student: ")
                    .append(record.getStudentName())
                    .append("\n");

            sb.append("Lecture: ")
                    .append(record.getLectureNo())
                    .append("\n");

            sb.append("Time: ")
                    .append(record.getDate())
                    .append("\n");

            sb.append("Correct: ")
                    .append(record.getCorrectAnswers())
                    .append("\n");

            sb.append("Wrong: ")
                    .append(record.getWrongAnswers())
                    .append("\n");

            sb.append("Status: ")
                    .append(record.getAttendanceStatus())
                    .append("\n");

            sb.append("----------------------------\n");
        }

        return sb.toString();
    }

    public static String exportAttendanceExcel() {

        try {

            File folder = new File("exports");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            String filename =
                    "exports/Attendance_" +
                            LocalDateTime.now().format(
                                    DateTimeFormatter.ofPattern(
                                            "dd-MM-yyyy_HH-mm"
                                    )
                            ) +
                            ".xls";

            PrintWriter writer =
                    new PrintWriter(filename);

            writer.println("<html>");
            writer.println("<head>");
            writer.println("<meta charset='UTF-8'>");
            writer.println("</head>");
            writer.println("<body>");

            writer.println("<h2>Attendance History</h2>");

            writer.println("<table border='1'>");

            writer.println("<tr>");
            writer.println("<th>Student</th>");
            writer.println("<th>Lecture</th>");
            writer.println("<th>Time</th>");
            writer.println("<th>Correct</th>");
            writer.println("<th>Wrong</th>");
            writer.println("<th>Status</th>");
            writer.println("</tr>");

            for (AttendanceRecord record : getAttendanceHistory()) {

                writer.println("<tr>");

                writer.println(
                        "<td>" +
                                escapeHTML(record.getStudentName()) +
                                "</td>"
                );

                writer.println(
                        "<td>" +
                                record.getLectureNo() +
                                "</td>"
                );

                writer.println(
                        "<td>" +
                                escapeHTML(record.getDate()) +
                                "</td>"
                );

                writer.println(
                        "<td>" +
                                record.getCorrectAnswers() +
                                "</td>"
                );

                writer.println(
                        "<td>" +
                                record.getWrongAnswers() +
                                "</td>"
                );

                writer.println(
                        "<td>" +
                                escapeHTML(record.getAttendanceStatus()) +
                                "</td>"
                );

                writer.println("</tr>");
            }

            writer.println("</table>");
            writer.println("</body>");
            writer.println("</html>");

            writer.close();

            return filename;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String exportAttendanceBoth() {

        String excelFile =
                exportAttendanceExcel();

        if (excelFile == null) {
            return "Export failed.";
        }

        return "Attendance exported successfully.\n\n" +
                "Excel File:\n" + excelFile;
    }

    private static String escapeHTML(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}