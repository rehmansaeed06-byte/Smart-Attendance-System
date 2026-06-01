package server;

import database.UserValidator;
import models.StudentQuizState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    private String role = "UNKNOWN";
    private String username = "";
    private String studentName = "Unknown Student";

    private StudentQuizState studentState;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            role = input.readUTF();

            if (role.equalsIgnoreCase("CHANGE_TEACHER_PASSWORD")) {
                handlePasswordReset("TEACHER");
                closeSocket();
                return;
            }

            if (role.equalsIgnoreCase("CHANGE_STUDENT_PASSWORD")) {
                handlePasswordReset("STUDENT");
                closeSocket();
                return;
            }

            username = input.readUTF();
            String password = input.readUTF();

            if (role.equalsIgnoreCase("TEACHER")) {
                // FIX: Require existing password to verify identity before allowing password change
                if (!UserValidator.validateTeacher(username, password)) {
                    sendMessage("LOGIN_FAILED");
                    closeSocket();
                    return;
                }
                sendMessage("LOGIN_SUCCESS");
                handleTeacher();

            } else if (role.equalsIgnoreCase("STUDENT")) {
                if (!UserValidator.validateStudent(username, password)) {
                    sendMessage("LOGIN_FAILED");
                    closeSocket();
                    return;
                }

                studentName = username;

                if (ServerMain.isStudentAlreadyConnected(studentName)) {
                    sendMessage("LOGIN_FAILED");
                    closeSocket();
                    return;
                }

                studentState = ServerMain.getStudentState(studentName);
                ServerMain.markStudentConnected(studentName);
                sendMessage("LOGIN_SUCCESS");
                handleStudent();

            } else {
                sendMessage("LOGIN_FAILED");
                closeSocket();
            }

        } catch (Exception e) {
            System.out.println(role + " disconnected.");
        } finally {
            ServerMain.removeClient(this);
            closeSocket();
        }
    }

    // FIX: Password reset now requires the old password for verification
    private void handlePasswordReset(String userType) {
        try {
            String resetUsername = input.readUTF();
            String oldPassword = input.readUTF();
            String newPassword = input.readUTF();

            // Verify old password first
            boolean verified;
            if (userType.equalsIgnoreCase("TEACHER")) {
                verified = UserValidator.validateTeacher(resetUsername, oldPassword);
            } else {
                verified = UserValidator.validateStudent(resetUsername, oldPassword);
            }

            if (!verified) {
                sendMessage("PASSWORD_CHANGE_FAILED");
                return;
            }

            if (newPassword.length() < 4) {
                sendMessage("PASSWORD_TOO_SHORT");
                return;
            }

            boolean changed;
            if (userType.equalsIgnoreCase("TEACHER")) {
                changed = UserValidator.changeTeacherPassword(resetUsername, newPassword);
            } else {
                changed = UserValidator.changeStudentPassword(resetUsername, newPassword);
            }

            sendMessage(changed ? "PASSWORD_CHANGE_SUCCESS" : "PASSWORD_CHANGE_FAILED");

        } catch (Exception e) {
            sendMessage("PASSWORD_CHANGE_FAILED");
        }
    }

    private void handleTeacher() throws Exception {
        System.out.println("Teacher connected: " + username);
        sendMessage("Welcome " + username + ". You can control the lecture now.");

        // FIX: Proper loop with graceful disconnect handling
        try {
            while (!socket.isClosed()) {
                String command = input.readUTF();

                if (command.equals("SERVER_SHUTDOWN")) break;

                if (command.startsWith("CHAT:")) {
                    String chatMessage = command.substring(5);
                    ServerMain.broadcastChatToStudents("Teacher: " + chatMessage);
                    sendMessage("CHAT:You: " + chatMessage);

                } else if (command.equalsIgnoreCase("/list")) {
                    sendMessage(ServerMain.getConnectedStudentsText());

                } else if (command.equalsIgnoreCase("/liveresponses")) {
                    sendMessage("LIVE_RESPONSE_FULL:" + ServerMain.getLiveResponsesText());

                } else if (command.startsWith("/send ")) {
                    String message = command.substring(6);
                    ServerMain.broadcastToStudents("Teacher Announcement: " + message);
                    sendMessage("Announcement sent to students.");

                } else if (command.startsWith("/startlecture ")) {
                    try {
                        int lectureNo = Integer.parseInt(command.substring(14).trim());
                        if (lectureNo < 1) {
                            sendMessage("Lecture number must be positive.");
                        } else {
                            ServerMain.startLecture(lectureNo);
                            sendMessage("Lecture " + lectureNo + " started with 50 seconds per question.");
                        }
                    } catch (NumberFormatException e) {
                        sendMessage("Invalid lecture number. Use a positive integer.");
                    }

                } else if (command.equalsIgnoreCase("/start")) {
                    ServerMain.startLecture(1);
                    sendMessage("Lecture 1 started with 50 seconds per question.");

                } else if (command.equalsIgnoreCase("/stopquiz")) {
                    ServerMain.stopLecture();
                    sendMessage("Stop quiz command sent.");

                } else if (command.equalsIgnoreCase("/report")) {
                    sendMessage(ServerMain.getReportText());

                } else {
                    sendMessage("Unknown command.");
                }
            }
        } catch (Exception e) {
            // Client disconnected
        }
    }

    private void handleStudent() throws Exception {
        System.out.println(studentName + " joined the classroom.");
        sendMessage("Welcome " + studentName + "! You are connected.");

        try {
            while (!socket.isClosed()) {
                String message = input.readUTF();

                if (message.equals("SERVER_SHUTDOWN")) break;

                if (message.startsWith("CHAT:")) {
                    String chatMessage = message.substring(5);
                    ServerMain.broadcastChatToTeachers(studentName + ": " + chatMessage);
                    sendMessage("CHAT:You: " + chatMessage);

                } else if (ServerMain.isQuestionActive) {
                    if (studentState.hasAnsweredCurrentQuestion()) {
                        sendMessage("You already answered this question.");
                    } else {
                        checkAnswer(message);
                    }
                } else {
                    sendMessage("No active question right now.");
                }
            }
        } catch (Exception e) {
            // Client disconnected
        }
    }

    private void checkAnswer(String message) {
        try {
            int studentAnswer = Integer.parseInt(message.trim());

            if (studentAnswer < 1 || studentAnswer > 4) {
                sendMessage("Invalid answer. Enter only 1, 2, 3, or 4.");
                return;
            }

            if (ServerMain.currentQuestion == null) {
                sendMessage("No active question found.");
                return;
            }

            String selectedOption = ServerMain.currentQuestion.getOptionText(studentAnswer);
            String correctOption = ServerMain.currentQuestion.getOptionText(ServerMain.currentCorrectAnswer);
            boolean isCorrect = studentAnswer == ServerMain.currentCorrectAnswer;

            if (isCorrect) {
                studentState.addCorrectAnswer();
                sendMessage("Correct answer!\nYour answer: " + studentAnswer + ". " + selectedOption);
                System.out.println(studentName + " answered correctly.");
            } else {
                studentState.addWrongAnswer();
                sendMessage(
                    "Wrong answer.\nYour answer: " + studentAnswer + ". " + selectedOption +
                    "\nCorrect answer: " + ServerMain.currentCorrectAnswer + ". " + correctOption
                );
                System.out.println(studentName + " answered wrong.");
            }

            ServerMain.addLiveResponse(studentName, studentAnswer, selectedOption, isCorrect);

        } catch (NumberFormatException e) {
            sendMessage("Invalid answer. Enter only 1, 2, 3, or 4.");
        }
    }

    public void sendMessage(String message) {
        try {
            if (output != null && !socket.isClosed()) {
                output.writeUTF(message);
                output.flush();
            }
        } catch (Exception e) {
            // Socket closed or broken
        }
    }

    // FIX: Added proper socket closing method
    public void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    public void resetAnswerStatus() {
        if (studentState != null) {
            studentState.resetAnswerStatus();
        }
    }

    public boolean isStudent() {
        return role.equalsIgnoreCase("STUDENT");
    }

    public boolean isTeacher() {
        return role.equalsIgnoreCase("TEACHER");
    }

    public String getStudentName() {
        return studentName;
    }

    public int getCorrectAnswers() {
        return studentState == null ? 0 : studentState.getCorrectAnswers();
    }

    public int getWrongAnswers() {
        return studentState == null ? 0 : studentState.getWrongAnswers();
    }

    public int getTotalAnswered() {
        return studentState == null ? 0 : studentState.getTotalAnswered();
    }

    public String getAttendanceStatus() {
        return studentState == null ? "Absent" : ServerMain.getAttendanceStatus(studentState);
    }
}
