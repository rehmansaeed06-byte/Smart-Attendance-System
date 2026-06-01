package gui;

import database.AttendanceDAO;
import database.LectureDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class TeacherGUI extends Application {

    private Stage window;
    private TextField serverIpField;
    private TextField teacherUsernameField;
    private PasswordField teacherPasswordField;
    private ComboBox<String> lectureBox;
    private VBox contentPanel;
    private TextArea currentContentArea;
    private TextArea liveResponsesArea;
    private TextArea quizMonitorArea;
    private TextArea chatArea;
    private TextField chatField;

    private final ArrayList<String> chatHistory = new ArrayList<>();
    private final ArrayList<String> liveResponsesHistory = new ArrayList<>();

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    @Override
    public void start(Stage stage) {
        window = stage;
        window.setTitle("Teacher Dashboard");
        window.setMaximized(true);
        showLoginPage();
        window.centerOnScreen();
        window.show();
    }

    private void showLoginPage() {
        Label title = new Label("Teacher Login");
        title.setStyle(titleStyle());

        serverIpField = new TextField("localhost");
        serverIpField.setPromptText("Server IP");
        serverIpField.setMaxWidth(300);

        teacherUsernameField = new TextField();
        teacherUsernameField.setPromptText("Teacher Username");
        teacherUsernameField.setMaxWidth(300);

        teacherPasswordField = new PasswordField();
        teacherPasswordField.setPromptText("Teacher Password");
        teacherPasswordField.setMaxWidth(300);

        Button connectBtn = new Button("Connect as Teacher");
        connectBtn.setPrefWidth(300);
        connectBtn.setStyle(buttonStyle());
        connectBtn.setOnAction(e -> connectToServer());

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setPrefWidth(300);
        changePasswordBtn.setStyle(buttonStyle());
        changePasswordBtn.setOnAction(e -> showChangePasswordDialog());

        VBox root = new VBox(20, title, serverIpField, teacherUsernameField, teacherPasswordField,
                             connectBtn, changePasswordBtn);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1B211A;");
        window.setScene(new Scene(root, 1200, 750));
    }

    private void showDashboard() {
        Label title = new Label("Teacher Dashboard");
        title.setStyle(titleStyle());

        Button lectureBtn = createMenuButton("Start Lecture");
        Button monitorBtn = createMenuButton("Quiz Monitor");
        Button connectedBtn = createMenuButton("Connected Students");
        Button attendanceBtn = createMenuButton("Attendance History");
        Button liveBtn = createMenuButton("Live Responses");
        Button chatBtn = createMenuButton("Chat");
        Button logoutBtn = createMenuButton("Logout");

        lectureBtn.setOnAction(e -> showLectureScreen());
        monitorBtn.setOnAction(e -> showQuizMonitorScreen());
        connectedBtn.setOnAction(e -> showConnectedStudentsScreen());
        attendanceBtn.setOnAction(e -> showAttendanceScreen());
        liveBtn.setOnAction(e -> showLiveResponsesScreen());
        chatBtn.setOnAction(e -> showChatScreen());
        logoutBtn.setOnAction(e -> logout());

        VBox buttonBox = new VBox(15, lectureBtn, monitorBtn, connectedBtn,
                                   attendanceBtn, liveBtn, chatBtn, logoutBtn);
        buttonBox.setAlignment(Pos.TOP_CENTER);

        VBox leftPanel = new VBox(25, title, buttonBox);
        leftPanel.setPrefWidth(280);
        leftPanel.setMinWidth(280);
        leftPanel.setMaxWidth(280);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setStyle("-fx-background-color: #1B211A; -fx-border-color: white; -fx-border-width: 0 1 0 0;");

        contentPanel = new VBox(18);
        contentPanel.setAlignment(Pos.CENTER);
        contentPanel.setPadding(new Insets(25));
        contentPanel.setPrefWidth(700);
        contentPanel.setPrefHeight(470);
        contentPanel.setMinHeight(470);
        contentPanel.setMaxHeight(470);
        contentPanel.setTranslateY(20);
        contentPanel.setStyle("-fx-background-color: #5A7863; -fx-border-color: white; -fx-border-radius: 12; -fx-background-radius: 12;");

        HBox dashboardLayout = new HBox(25, leftPanel, contentPanel);
        dashboardLayout.setPadding(new Insets(20));
        dashboardLayout.setAlignment(Pos.TOP_LEFT);
        dashboardLayout.setStyle("-fx-background-color: #1B211A;");
        HBox.setHgrow(contentPanel, Priority.ALWAYS);

        window.setScene(new Scene(dashboardLayout));
        showLectureScreen();
    }

    private void showLectureScreen() {
        contentPanel.getChildren().clear();
        currentContentArea = null;
        liveResponsesArea = null;
        quizMonitorArea = null;

        Label heading = new Label("Start Lecture");
        heading.setStyle(sectionTitleStyle());

        lectureBox = new ComboBox<>();
        lectureBox.getItems().addAll(LectureDAO.getAllLectures());
        if (!lectureBox.getItems().isEmpty()) {
            lectureBox.setValue(lectureBox.getItems().get(0));
        }
        lectureBox.setPrefWidth(380);
        lectureBox.setPrefHeight(40);

        Button startBtn = new Button("Start Quiz");
        startBtn.setPrefWidth(180);
        startBtn.setPrefHeight(45);
        startBtn.setStyle(buttonStyle());
        startBtn.setOnAction(e -> startSelectedLecture());

        Button stopBtn = new Button("Stop Quiz");
        stopBtn.setPrefWidth(180);
        stopBtn.setPrefHeight(45);
        stopBtn.setStyle(buttonStyle());
        stopBtn.setOnAction(e -> sendCommand("/stopquiz"));

        HBox buttons = new HBox(20, startBtn, stopBtn);
        buttons.setAlignment(Pos.CENTER);

        TextArea infoArea = createPanelTextArea();
        currentContentArea = infoArea;
        infoArea.setText(
            "Instructions:\n\n" +
            "1. Select a lecture from dropdown.\n" +
            "2. Click Start Quiz.\n" +
            "3. Students will receive questions.\n" +
            "4. Click Stop Quiz anytime to end quiz and save attendance.\n\n" +
            "Note: Attendance is only shown in Attendance History."
        );

        contentPanel.getChildren().addAll(heading, lectureBox, buttons, infoArea);
    }

    private void showQuizMonitorScreen() {
        contentPanel.getChildren().clear();
        currentContentArea = null;
        liveResponsesArea = null;

        Label heading = new Label("Quiz Monitor");
        heading.setStyle(sectionTitleStyle());

        quizMonitorArea = createPanelTextArea();
        quizMonitorArea.setText("Quiz Monitor\n----------------------------\nWaiting for quiz to start...");

        contentPanel.getChildren().addAll(heading, quizMonitorArea);
    }

    private void showConnectedStudentsScreen() {
        contentPanel.getChildren().clear();
        liveResponsesArea = null;
        quizMonitorArea = null;

        Label heading = new Label("Connected Students");
        heading.setStyle(sectionTitleStyle());

        TextArea area = createPanelTextArea();
        currentContentArea = area;
        area.setText("Click Refresh to load connected students.\n");

        Button refreshBtn = new Button("Refresh Connected Students");
        refreshBtn.setPrefWidth(250);
        refreshBtn.setStyle(buttonStyle());
        refreshBtn.setOnAction(e -> {
            area.clear();
            sendCommand("/list");
        });

        contentPanel.getChildren().addAll(heading, refreshBtn, area);
    }

    private void showAttendanceScreen() {
        contentPanel.getChildren().clear();
        liveResponsesArea = null;
        quizMonitorArea = null;
        currentContentArea = null;

        Label heading = new Label("Attendance History");
        heading.setStyle(sectionTitleStyle());

        TextArea area = createPanelTextArea();
        area.setText(AttendanceDAO.getAttendanceHistoryText());

        Button refreshBtn = new Button("Refresh Attendance");
        refreshBtn.setPrefWidth(220);
        refreshBtn.setStyle(buttonStyle());
        refreshBtn.setOnAction(e -> area.setText(AttendanceDAO.getAttendanceHistoryText()));

        Button exportBtn = new Button("Export Attendance");
        exportBtn.setPrefWidth(220);
        exportBtn.setStyle(buttonStyle());
        exportBtn.setOnAction(e -> showAlert(AttendanceDAO.exportAttendanceBoth()));

        HBox buttonRow = new HBox(20, refreshBtn, exportBtn);
        buttonRow.setAlignment(Pos.CENTER);

        contentPanel.getChildren().addAll(heading, buttonRow, area);
    }

    private void showLiveResponsesScreen() {
        contentPanel.getChildren().clear();
        currentContentArea = null;
        quizMonitorArea = null;

        Label heading = new Label("Live Responses");
        heading.setStyle(sectionTitleStyle());

        liveResponsesArea = createPanelTextArea();
        if (liveResponsesHistory.isEmpty()) {
            liveResponsesArea.setText(
                "Live Responses\n----------------------------\nNo live responses yet.\n\n" +
                "When students submit answers during a quiz,\ntheir responses will appear here."
            );
        } else {
            liveResponsesArea.setText("Live Responses\n----------------------------\n");
            for (String response : liveResponsesHistory) {
                liveResponsesArea.appendText(response + "\n");
            }
        }

        Button refreshBtn = new Button("Refresh Live Responses");
        refreshBtn.setPrefWidth(230);
        refreshBtn.setStyle(buttonStyle());
        refreshBtn.setOnAction(e -> sendCommand("/liveresponses"));

        contentPanel.getChildren().addAll(heading, refreshBtn, liveResponsesArea);
    }

    private void showChatScreen() {
        contentPanel.getChildren().clear();
        currentContentArea = null;
        liveResponsesArea = null;
        quizMonitorArea = null;

        Label heading = new Label("Chat");
        heading.setStyle(sectionTitleStyle());

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefWidth(380);
        chatArea.setPrefHeight(240);
        chatArea.setStyle("-fx-font-size: 14px; -fx-control-inner-background: white; -fx-text-fill: black;");

        for (String msg : chatHistory) chatArea.appendText(msg + "\n");

        chatField = new TextField();
        chatField.setPromptText("Type message");
        chatField.setPrefWidth(300);
        chatField.setPrefHeight(40);
        chatField.setOnAction(e -> sendChatMessage());

        Button sendBtn = new Button("Send");
        sendBtn.setPrefWidth(90);
        sendBtn.setPrefHeight(40);
        sendBtn.setStyle(buttonStyle());
        sendBtn.setOnAction(e -> sendChatMessage());

        HBox inputRow = new HBox(10, chatField, sendBtn);
        inputRow.setAlignment(Pos.CENTER);

        VBox chatBox = new VBox(12, chatArea, inputRow);
        chatBox.setAlignment(Pos.CENTER);
        chatBox.setPadding(new Insets(15));
        chatBox.setPrefWidth(430);
        chatBox.setStyle(
            "-fx-background-color: #1B211A; -fx-border-color: white; " +
            "-fx-border-radius: 12; -fx-background-radius: 12;"
        );

        contentPanel.getChildren().addAll(heading, chatBox);
    }

    private void startSelectedLecture() {
        String selectedLecture = lectureBox.getValue();
        if (selectedLecture == null) {
            showAlert("Please select a lecture.");
            return;
        }
        int lectureNo = LectureDAO.extractLectureNo(selectedLecture);
        if (lectureNo == -1) {
            showAlert("Invalid lecture selected.");
            return;
        }
        liveResponsesHistory.clear();
        if (liveResponsesArea != null) {
            liveResponsesArea.clear();
            liveResponsesArea.setText("Live Responses\n----------------------------\n");
        }
        sendCommand("/startlecture " + lectureNo);
    }

    private void connectToServer() {
        try {
            String serverIP = serverIpField.getText().trim();
            String username = teacherUsernameField.getText().trim();
            String password = teacherPasswordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Please enter username and password.");
                return;
            }
            if (serverIP.isEmpty()) serverIP = "localhost";

            socket = new Socket(serverIP, Integer.parseInt(System.getenv().getOrDefault("PORT", "5000")));
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            output.writeUTF("TEACHER");
            output.writeUTF(username);
            output.writeUTF(password);
            output.flush();

            String response = input.readUTF();
            if (response.equals("LOGIN_SUCCESS")) {
                showDashboard();
                startReceiverThread();
            } else {
                showAlert("Invalid username or password.");
                socket.close();
            }

        } catch (Exception e) {
            showAlert("Could not connect to server: " + e.getMessage());
        }
    }

    private void startReceiverThread() {
        Thread receiver = new Thread(() -> {
            try {
                while (true) {
                    String message = input.readUTF();
                    if (message.equals("SERVER_SHUTDOWN")) {
                        Platform.runLater(() -> {
                            showAlert("Server has shut down.");
                            logout();
                        });
                        break;
                    }

                    Platform.runLater(() -> {
                        if (message.startsWith("CHAT:")) {
                            String chatMsg = message.substring(5);
                            chatHistory.add(chatMsg);
                            if (chatArea != null) chatArea.appendText(chatMsg + "\n");

                        } else if (message.startsWith("LIVE_RESPONSE:")) {
                            String response = message.substring("LIVE_RESPONSE:".length());
                            liveResponsesHistory.add(response);
                            if (liveResponsesArea != null) liveResponsesArea.appendText(response + "\n");

                        } else if (message.startsWith("LIVE_RESPONSE_FULL:")) {
                            String full = message.substring("LIVE_RESPONSE_FULL:".length());
                            if (liveResponsesArea != null) {
                                liveResponsesArea.clear();
                                liveResponsesArea.setText(full);
                            }

                        } else if (message.startsWith("QUIZ_MONITOR:")) {
                            String monitorData = message.substring("QUIZ_MONITOR:".length());
                            if (quizMonitorArea != null) {
                                quizMonitorArea.clear();
                                quizMonitorArea.setText(monitorData);
                            }

                        } else {
                            showMessageInContent(message);
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showMessageInContent("Disconnected from server."));
            }
        });
        receiver.setDaemon(true);
        receiver.start();
    }

    private void showMessageInContent(String message) {
        if (contentPanel == null) return;
        if (currentContentArea != null) currentContentArea.appendText(message + "\n\n");
    }

    private void sendCommand(String command) {
        try {
            if (output == null) {
                showAlert("Not connected to server.");
                return;
            }
            output.writeUTF(command);
            output.flush();
        } catch (Exception e) {
            showAlert("Could not send command.");
        }
    }

    private void sendChatMessage() {
        if (chatField == null) return;
        String msg = chatField.getText().trim();
        if (!msg.isEmpty()) {
            sendCommand("CHAT:" + msg);
            chatField.clear();
        }
    }

    private void logout() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        socket = null;
        input = null;
        output = null;
        chatHistory.clear();
        liveResponsesHistory.clear();
        showLoginPage();
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(50);
        btn.setStyle(buttonStyle());
        return btn;
    }

    private TextArea createPanelTextArea() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefWidth(620);
        area.setPrefHeight(300);
        area.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-control-inner-background: white; -fx-text-fill: black;");
        return area;
    }

    private String titleStyle() {
        return "-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;";
    }

    private String sectionTitleStyle() {
        return "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;";
    }

    private String buttonStyle() {
        return "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 12; " +
               "-fx-background-color: #5A7863; -fx-border-color: white; " +
               "-fx-border-radius: 12; -fx-text-fill: white;";
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // FIX: Password change dialog now requires old password for verification
    private void showChangePasswordDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Change Password");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Teacher Username");

        PasswordField oldPassField = new PasswordField();
        oldPassField.setPromptText("Current Password");

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("New Password (min 4 chars)");

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm New Password");

        Button changeBtn = new Button("Change Password");
        changeBtn.setStyle(buttonStyle());

        VBox box = new VBox(12,
            new Label("Teacher Username"), usernameField,
            new Label("Current Password"), oldPassField,
            new Label("New Password"), newPassField,
            new Label("Confirm Password"), confirmPassField,
            changeBtn
        );
        box.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        changeBtn.setOnAction(e -> {
            try {
                String serverIP = serverIpField.getText().trim();
                if (serverIP.isEmpty()) serverIP = "localhost";
                String username = usernameField.getText().trim();
                String oldPass = oldPassField.getText();
                String newPass = newPassField.getText();
                String confirmPass = confirmPassField.getText();

                if (username.isEmpty() || oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    showAlert("All fields are required.");
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    showAlert("New passwords do not match.");
                    return;
                }
                if (newPass.length() < 4) {
                    showAlert("Password must be at least 4 characters.");
                    return;
                }

                Socket tempSocket = new Socket(serverIP, Integer.parseInt(System.getenv().getOrDefault("PORT", "5000")));
                DataOutputStream tempOut = new DataOutputStream(tempSocket.getOutputStream());
                DataInputStream tempIn = new DataInputStream(tempSocket.getInputStream());

                tempOut.writeUTF("CHANGE_TEACHER_PASSWORD");
                tempOut.writeUTF(username);
                tempOut.writeUTF(oldPass);
                tempOut.writeUTF(newPass);
                tempOut.flush();

                String response = tempIn.readUTF();
                tempSocket.close();
                dialog.close();

                if (response.equals("PASSWORD_CHANGE_SUCCESS")) {
                    showAlert("Password changed successfully.");
                } else if (response.equals("PASSWORD_TOO_SHORT")) {
                    showAlert("Password must be at least 4 characters.");
                } else {
                    showAlert("Password change failed. Check username and current password.");
                }

            } catch (Exception ex) {
                showAlert("Could not connect to server.");
            }
        });

        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
