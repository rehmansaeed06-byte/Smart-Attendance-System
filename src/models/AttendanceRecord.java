package models;

public class AttendanceRecord {

    private String studentName;
    private int lectureNo;
    private String date;
    private int correctAnswers;
    private int wrongAnswers;
    private String attendanceStatus;

    public AttendanceRecord(String studentName,
                            int lectureNo,
                            String date,
                            int correctAnswers,
                            int wrongAnswers,
                            String attendanceStatus) {

        this.studentName = studentName;
        this.lectureNo = lectureNo;
        this.date = date;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.attendanceStatus = attendanceStatus;
    }

    public String getStudentName() {
        return studentName;
    }

    public int getLectureNo() {
        return lectureNo;
    }

    public String getDate() {
        return date;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }
}