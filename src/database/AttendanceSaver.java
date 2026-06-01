package database;

import server.ClientHandler;
import server.ServerMain;

import java.io.FileWriter;

public class AttendanceSaver {

    public static void saveReport() {

        try {

            FileWriter writer =
                    new FileWriter("attendance_report.txt");

            writer.write("Attendance Report\n");
            writer.write("----------------------------\n");

            for (ClientHandler client : ServerMain.clients) {

                if (client.isStudent()) {

                    writer.write("Student: "
                            + client.getStudentName() + "\n");

                    writer.write("Correct: "
                            + client.getCorrectAnswers() + "\n");

                    writer.write("Wrong: "
                            + client.getWrongAnswers() + "\n");

                    writer.write("Answered: "
                            + client.getTotalAnswered() + "\n");

                    writer.write("Status: "
                            + client.getAttendanceStatus() + "\n");

                    writer.write("----------------------------\n");
                }
            }

            writer.close();

            System.out.println("Attendance report saved.");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}