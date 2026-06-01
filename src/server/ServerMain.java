package server;

import database.AttendanceDAO;
import database.AttendanceSaver;
import database.QuestionDAO;
import gui.ServerGUI;
import models.Question;
import models.StudentQuizState;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMain {

    public static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    public static List<Question> questions = new ArrayList<>();

    public static final ConcurrentHashMap<String, StudentQuizState> studentStates = new ConcurrentHashMap<>();
    public static final List<String> liveResponses = new CopyOnWriteArrayList<>();

    public static volatile boolean isQuestionActive = false;
    public static volatile boolean quizRunning = false;

    public static volatile int currentCorrectAnswer = 0;
    public static volatile Question currentQuestion;
    public static volatile int totalQuestions = 0;
    public static volatile int currentLectureNo = 1;
    public static volatile int currentQuestionNo = 0;

    private static ServerSocket serverSocket;
    private static volatile boolean serverRunning = false;

    public static ServerGUI gui;

    public static void setGUI(ServerGUI serverGUI) {
        gui = serverGUI;
    }

    public static boolean isServerRunning() {
        return serverRunning;
    }

    public static synchronized StudentQuizState getStudentState(String studentName) {
        return studentStates.computeIfAbsent(studentName, k -> new StudentQuizState());
    }

    public static synchronized boolean isStudentAlreadyConnected(String studentName) {
        StudentQuizState state = studentStates.get(studentName);
        return state != null && state.isConnected();
    }

    public static synchronized void markStudentConnected(String studentName) {
        getStudentState(studentName).markConnected();
    }

    public static synchronized void markStudentDisconnected(String studentName) {
        StudentQuizState state = studentStates.get(studentName);
        if (state != null) {
            state.markDisconnected();
        }
    }

    public static synchronized void addLiveResponse(
            String studentName,
            int answerNo,
            String selectedOption,
            boolean correct
    ) {
        String status = correct ? "Correct" : "Wrong";
        String response =
                "Student: " + studentName + "\n" +
                "Answer: " + answerNo + ". " + selectedOption + "\n" +
                "Result: " + status + "\n" +
                "----------------------------";

        liveResponses.add(response);
        broadcastToTeachers("LIVE_RESPONSE:" + response);
    }

    public static String getLiveResponsesText() {
        if (liveResponses.isEmpty()) {
            return "No live responses yet.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Live Responses\n");
        sb.append("----------------------------\n");
        for (String response : liveResponses) {
            sb.append(response).append("\n");
        }
        return sb.toString();
    }

    public static void startServer() {
        if (serverRunning) {
            log("Server is already running.");
            return;
        }

        serverRunning = true;

        Thread serverThread = new Thread(() -> {
            int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "5000")
            );

            try {
                serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
                String localIP = InetAddress.getLocalHost().getHostAddress();

                log("SERVER STARTED SUCCESSFULLY");
                log("Host: " + localIP);
                log("Port: " + port);
                log("Waiting for teacher and students...");

                while (serverRunning) {
                    Socket socket = serverSocket.accept();
                    log("NEW CLIENT CONNECTED");
                    ClientHandler client = new ClientHandler(socket);
                    clients.add(client);
                    client.start();
                }

            } catch (Exception e) {
                if (serverRunning) {
                    log("Server Error: " + e.getMessage());
                }
            } finally {
                serverRunning = false;
                log("Server stopped.");
            }
        });

        serverThread.setDaemon(true);
        serverThread.start();
    }

    public static void stopServer() {
        try {
            serverRunning = false;
            quizRunning = false;
            isQuestionActive = false;
            currentCorrectAnswer = 0;
            currentQuestion = null;
            currentQuestionNo = 0;

            broadcastToStudents("SERVER_SHUTDOWN");
            broadcastToTeachers("SERVER_SHUTDOWN");

            for (ClientHandler client : clients) {
                client.closeSocket();
            }

            clients.clear();
            studentStates.clear();
            liveResponses.clear();

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            log("Server stopped successfully.");

        } catch (Exception e) {
            log("Error stopping server: " + e.getMessage());
        }
    }

    public static void log(String message) {
        System.out.println(message);
        if (gui != null) {
            gui.addLog(message);
        }
    }

    public static void startLecture(int lectureNo) {
        if (quizRunning) {
            broadcastToTeachers("Quiz is already running.");
            return;
        }

        if (lectureNo < 1) {
            broadcastToTeachers("Invalid lecture number: " + lectureNo);
            return;
        }

        currentLectureNo = lectureNo;
        currentQuestionNo = 0;
        questions = QuestionDAO.getQuestionsByLecture(lectureNo);
        totalQuestions = questions.size();

        if (questions.isEmpty()) {
            broadcastToTeachers("No questions found for Lecture " + lectureNo);
            sendQuizMonitorUpdate("No questions found", "Lecture " + lectureNo + " has no questions.", 0);
            return;
        }

        liveResponses.clear();

        for (StudentQuizState state : studentStates.values()) {
            state.resetAnswerStatus();
        }

        quizRunning = true;
        broadcastToTeachers("Lecture " + lectureNo + " started.");
        broadcastToStudents("Lecture " + lectureNo + " quiz started.");
        sendQuizMonitorUpdate("Quiz Started",
            "Lecture " + lectureNo + " started with " + totalQuestions + " questions.", 50);

        Thread questionThread = new Thread(() -> {
            try {
                for (int i = 0; i < questions.size(); i++) {
                    if (!quizRunning) break;

                    Question question = questions.get(i);
                    currentQuestionNo = i + 1;
                    currentQuestion = question;
                    currentCorrectAnswer = question.getCorrectOption();

                    // Reset answer flags BEFORE activating the question
                    for (StudentQuizState state : studentStates.values()) {
                        state.resetAnswerStatus();
                    }

                    // Activate question AFTER resetting flags to prevent race condition
                    isQuestionActive = true;

                    broadcastToStudents(question.formatQuestion());

                    // FIX: Send the full question text ONCE to teachers, then only send
                    // lightweight timer ticks — avoids broadcasting the full question
                    // string every second which was causing noticeable delay for students
                    // trying to submit answers (their sendMessage had to wait for the
                    // large broadcast to finish on every tick).
                    sendQuizMonitorUpdate("Question Active", question.formatQuestion(), 50);

                    for (int remaining = 49; remaining >= 1; remaining--) {
                        if (!quizRunning) break;
                        Thread.sleep(1000);
                        // Send only timer tick — small payload, minimal blocking
                        sendTimerTick(remaining);
                    }
                    // Sleep the final second
                    if (quizRunning) Thread.sleep(1000);

                    isQuestionActive = false;
                    if (!quizRunning) break;

                    broadcastToStudents("Time is up! Next question will appear soon.");
                    sendQuizMonitorUpdate("Time Up", "Question " + currentQuestionNo + " time finished.", 0);
                    Thread.sleep(2000);
                }

                finishQuiz("Lecture questions finished.");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        questionThread.setDaemon(true);
        questionThread.start();
    }

    /**
     * FIX: Lightweight timer-only update sent each second during a question.
     * Replaces the previous approach of re-sending the entire question text
     * every second, which caused blocking delay for student answer submissions.
     */
    public static void sendTimerTick(int remainingSeconds) {
        String msg =
            "QUIZ_MONITOR:" +
            "Status: Question Active\n" +
            "Lecture: " + currentLectureNo + "\n" +
            "Question: " + currentQuestionNo + " / " + totalQuestions + "\n" +
            "Timer: " + remainingSeconds + " seconds\n" +
            "Connected Students: " + getConnectedStudentCount() + "\n\n" +
            "[Question unchanged - timer update only]";
        broadcastToTeachers(msg);
    }

    public static void stopLecture() {
        if (!quizRunning) {
            broadcastToTeachers("No active quiz to stop.");
            sendQuizMonitorUpdate("No Active Quiz", "There is no running quiz to stop.", 0);
            return;
        }

        quizRunning = false;
        isQuestionActive = false;
        broadcastToStudents("Quiz stopped by teacher.");
        broadcastToTeachers("Quiz stopped manually.");
        finishQuiz("Quiz stopped manually.");
    }

    private static void finishQuiz(String message) {
        quizRunning = false;
        isQuestionActive = false;
        currentQuestion = null;
        currentCorrectAnswer = 0;

        broadcastToStudents(message);
        broadcastToTeachers(message);
        sendQuizMonitorUpdate("Quiz Finished", message, 0);

        saveAttendanceToDatabase();
        AttendanceSaver.saveReport();
    }

    private static void saveAttendanceToDatabase() {
        for (String studentName : studentStates.keySet()) {
            StudentQuizState state = studentStates.get(studentName);
            AttendanceDAO.saveAttendance(
                studentName,
                currentLectureNo,
                state.getCorrectAnswers(),
                state.getWrongAnswers(),
                getAttendanceStatus(state)
            );
        }
        broadcastToTeachers("Attendance saved permanently in database.");
    }

    public static String getAttendanceStatus(StudentQuizState state) {
        if (totalQuestions == 0 || state.getTotalAnswered() == 0) {
            return "Absent";
        }
        double percentage = ((double) state.getCorrectAnswers() / totalQuestions) * 100;
        if (percentage >= 70) return "Present";
        else if (percentage >= 40) return "Late / Partial";
        else return "Absent";
    }

    public static void sendQuizMonitorUpdate(String status, String questionText, int remainingSeconds) {
        String msg =
            "QUIZ_MONITOR:" +
            "Status: " + status + "\n" +
            "Lecture: " + currentLectureNo + "\n" +
            "Question: " + currentQuestionNo + " / " + totalQuestions + "\n" +
            "Timer: " + remainingSeconds + " seconds\n" +
            "Connected Students: " + getConnectedStudentCount() + "\n\n" +
            questionText;
        broadcastToTeachers(msg);
    }

    public static int getConnectedStudentCount() {
        int count = 0;
        for (ClientHandler client : clients) {
            if (client.isStudent()) count++;
        }
        return count;
    }

    public static void broadcastToStudents(String message) {
        for (ClientHandler client : clients) {
            if (client.isStudent()) client.sendMessage(message);
        }
    }

    public static void broadcastToTeachers(String message) {
        for (ClientHandler client : clients) {
            if (client.isTeacher()) client.sendMessage(message);
        }
    }

    public static void broadcastChatToStudents(String chatMessage) {
        for (ClientHandler client : clients) {
            if (client.isStudent()) client.sendMessage("CHAT:" + chatMessage);
        }
    }

    public static void broadcastChatToTeachers(String chatMessage) {
        for (ClientHandler client : clients) {
            if (client.isTeacher()) client.sendMessage("CHAT:" + chatMessage);
        }
    }

    public static String getConnectedStudentsText() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nConnected Students:\n");
        sb.append("Total: ").append(getConnectedStudentCount()).append("\n");
        sb.append("----------------------------\n");
        boolean found = false;
        for (ClientHandler client : clients) {
            if (client.isStudent()) {
                sb.append("- ").append(client.getStudentName()).append("\n");
                found = true;
            }
        }
        if (!found) sb.append("No students connected.\n");
        return sb.toString();
    }

    public static String getReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nCurrent Quiz Summary\n");
        sb.append("Lecture: ").append(currentLectureNo).append("\n");
        sb.append("----------------------------\n");
        for (String studentName : studentStates.keySet()) {
            StudentQuizState state = studentStates.get(studentName);
            sb.append("Student: ").append(studentName).append("\n");
            sb.append("Correct: ").append(state.getCorrectAnswers()).append("\n");
            sb.append("Wrong: ").append(state.getWrongAnswers()).append("\n");
            sb.append("Answered: ").append(state.getTotalAnswered()).append("\n");
            sb.append("----------------------------\n");
        }
        return sb.toString();
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        if (client.isStudent()) {
            markStudentDisconnected(client.getStudentName());
        }
    }
}
