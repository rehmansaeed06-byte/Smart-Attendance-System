package gui;

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

public class StudentGUI extends Application {

    private Stage window;
    private TextField serverIpField;
    private TextField studentUsernameField;
    private PasswordField studentPasswordField;
    private TextArea messageArea;
    private TextArea chatArea;
    private TextField answerField;
    private TextField chatField;
    private VBox chatBox;

    private final ArrayList<String> chatHistory = new ArrayList<>();

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    @Override
    public void start(Stage stage) {
        window = stage;
        window.setTitle("Student Dashboard");
        window.setWidth(900);
        window.setHeight(600);
        window.setResizable(false);
        showLoginPage();
        window.centerOnScreen();
        window.show();
    }

    private void showLoginPage() {
        Label title = new Label("Student Login");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        serverIpField = new TextField("localhost");
        serverIpField.setPromptText("Server IP");
        serverIpField.setMaxWidth(260);

        studentUsernameField = new TextField();
        studentUsernameField.setPromptText("Student Username");
        studentUsernameField.setMaxWidth(260);

        studentPasswordField = new PasswordField();
        studentPasswordField.setPromptText("Student Password");
        studentPasswordField.setMaxWidth(260);

        Button connectBtn = new Button("Connect as Student");
        connectBtn.setPrefWidth(260);
        connectBtn.setStyle(buttonStyle());
        connectBtn.setOnAction(e -> connectToServer());

        // Allow pressing Enter on password field to connect
        studentPasswordField.setOnAction(e -> connectToServer());

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setPrefWidth(260);
        changePasswordBtn.setStyle(buttonStyle());
        changePasswordBtn.setOnAction(e -> showChangePasswordDialog());

        VBox root = new VBox(20, title, serverIpField, studentUsernameField,
                             studentPasswordField, connectBtn, changePasswordBtn);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1B211A;");
        window.setScene(new Scene(root, 900, 600));
    }

    private void showDashboard() {
        Label title = new Label("Student Dashboard");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button logoutBtn = createSideButton("Logout");
        Button chatBtn = createSideButton("CHAT BOX ▼");

        logoutBtn.setOnAction(e -> logout());
        chatBtn.setOnAction(e -> toggleChatBox());

        VBox leftMenu = new VBox(14, logoutBtn, chatBtn);
        leftMenu.setPadding(new Insets(15, 20, 10, 20));
        leftMenu.setPrefWidth(230);

        Label updatesTitle = new Label("Quiz & Notifications");
        updatesTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setPrefWidth(500);
        messageArea.setPrefHeight(250);
        messageArea.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-control-inner-background: white; -fx-text-fill: black;");

        answerField = new TextField();
        answerField.setPromptText("Enter answer: 1, 2, 3, or 4");
        answerField.setOnAction(e -> sendAnswer());

        Button submitAnswerBtn = new Button("Submit Answer");
        submitAnswerBtn.setPrefWidth(160);
        submitAnswerBtn.setStyle(buttonStyle());
        submitAnswerBtn.setOnAction(e -> sendAnswer());

        HBox answerRow = new HBox(10, answerField, submitAnswerBtn);
        HBox.setHgrow(answerField, Priority.ALWAYS);

        VBox updatesSection = new VBox(20, updatesTitle, messageArea, answerRow);
        updatesSection.setPadding(new Insets(10, 20, 10, 20));

        HBox topSection = new HBox(10, leftMenu, updatesSection);

        chatBox = createChatBox();
        chatBox.setVisible(false);
        chatBox.setManaged(false);

        VBox mainLayout = new VBox(20, title, topSection, chatBox);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #1B211A;");

        window.setScene(new Scene(mainLayout, 900, 600));
    }

    private VBox createChatBox() {
        Label chatTitle = new Label("Chat Box");
        chatTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefWidth(760);
        chatArea.setPrefHeight(80);

        chatField = new TextField();
        chatField.setPromptText("Type message to teacher");
        chatField.setOnAction(e -> sendChatMessage());

        Button sendBtn = new Button("Send");
        sendBtn.setPrefWidth(100);
        sendBtn.setStyle(buttonStyle());
        sendBtn.setOnAction(e -> sendChatMessage());

        HBox inputRow = new HBox(10, chatField, sendBtn);
        HBox.setHgrow(chatField, Priority.ALWAYS);

        VBox box = new VBox(8, chatTitle, chatArea, inputRow);
        box.setPadding(new Insets(10));
        box.setPrefWidth(800);
        box.setPrefHeight(165);
        box.setStyle("-fx-background-color: #5A7863; -fx-border-color: white; -fx-border-radius: 12; -fx-background-radius: 12;");

        return box;
    }

    private void connectToServer() {
        try {
            String serverIP = serverIpField.getText().trim();
            String username = studentUsernameField.getText().trim();
            String password = studentPasswordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Please enter username and password.");
                return;
            }
            if (serverIP.isEmpty()) serverIP = "localhost";

            socket = new Socket(serverIP, Integer.parseInt(System.getenv().getOrDefault("PORT", "5000")));
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            output.writeUTF("STUDENT");
            output.writeUTF(username);
            output.writeUTF(password);
            output.flush();

            String response = input.readUTF();
            if (response.equals("LOGIN_SUCCESS")) {
                showDashboard();
                startReceiverThread();
            } else {
                showAlert("Invalid username or password, or already connected.");
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
                        } else {
                            if (messageArea != null) messageArea.appendText(message + "\n\n");
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (messageArea != null) messageArea.appendText("Disconnected from server.\n");
                });
            }
        });
        receiver.setDaemon(true);
        receiver.start();
    }

    private void sendAnswer() {
        try {
            String answer = answerField.getText().trim();
            if (answer.isEmpty()) {
                showAlert("Please enter an answer.");
                return;
            }
            output.writeUTF(answer);
            output.flush();
            answerField.clear();
        } catch (Exception e) {
            showAlert("Could not send answer.");
        }
    }

    private void sendChatMessage() {
        String msg = chatField.getText().trim();
        if (!msg.isEmpty()) {
            sendCommand("CHAT:" + msg);
            chatField.clear();
        }
    }

    private void sendCommand(String command) {
        try {
            output.writeUTF(command);
            output.flush();
        } catch (Exception e) {
            showAlert("Could not send command.");
        }
    }

    private void toggleChatBox() {
        boolean show = !chatBox.isVisible();
        chatBox.setVisible(show);
        chatBox.setManaged(show);
        if (show) {
            chatArea.clear();
            for (String msg : chatHistory) chatArea.appendText(msg + "\n");
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
        showLoginPage();
    }

    private Button createSideButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(190);
        btn.setPrefHeight(38);
        btn.setStyle(buttonStyle());
        return btn;
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

    // FIX: Password change now requires old password for verification
    private void showChangePasswordDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Change Password");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Student Username");

        PasswordField oldPassField = new PasswordField();
        oldPassField.setPromptText("Current Password");

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("New Password (min 4 chars)");

        Button changeBtn = new Button("Change Password");
        changeBtn.setStyle(buttonStyle());

        VBox box = new VBox(12,
            new Label("Student Username"), usernameField,
            new Label("Current Password"), oldPassField,
            new Label("New Password"), newPassField,
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

                if (username.isEmpty() || oldPass.isEmpty() || newPass.isEmpty()) {
                    showAlert("All fields are required.");
                    return;
                }
                if (newPass.length() < 4) {
                    showAlert("Password must be at least 4 characters.");
                    return;
                }

                Socket tempSocket = new Socket(serverIP, Integer.parseInt(System.getenv().getOrDefault("PORT", "5000")));
                DataOutputStream tempOut = new DataOutputStream(tempSocket.getOutputStream());
                DataInputStream tempIn = new DataInputStream(tempSocket.getInputStream());

                tempOut.writeUTF("CHANGE_STUDENT_PASSWORD");
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
