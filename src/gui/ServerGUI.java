package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import server.ServerMain;

import java.net.InetAddress;

public class ServerGUI extends Application {

    private TextArea updatesArea;
    private Label hostLabel;
    private Label portLabel;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {

        stage.setTitle("Server Control Panel");
        stage.setWidth(900);
        stage.setHeight(600);
        stage.setResizable(false);

        ServerMain.setGUI(this);

        Label title = new Label("Server Control Panel");
        title.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Button startBtn = createButton("Start Server");
        Button stopBtn = createButton("Stop Server");

        startBtn.setOnAction(e -> {
            ServerMain.startServer();
            updateStatus();
        });

        stopBtn.setOnAction(e -> {
            ServerMain.stopServer();
            updateStatus();
        });

        VBox leftMenu = new VBox(18, startBtn, stopBtn);
        leftMenu.setPadding(new Insets(15, 20, 10, 20));
        leftMenu.setPrefWidth(230);

        Label infoTitle = new Label("Server Information");
        infoTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        hostLabel = new Label("Host/IP: --");
        portLabel = new Label("Port: 5000");
        statusLabel = new Label("Status: Stopped");

        hostLabel.setStyle(infoTextStyle());
        portLabel.setStyle(infoTextStyle());
        statusLabel.setStyle(infoTextStyle());

        VBox infoBox = new VBox(15,
                infoTitle,
                hostLabel,
                portLabel,
                statusLabel
        );

        infoBox.setPadding(new Insets(20));
        infoBox.setPrefWidth(500);
        infoBox.setPrefHeight(200);

        infoBox.setStyle(
                "-fx-background-color: #5A7863;" +
                        "-fx-border-color: white;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        HBox topSection = new HBox(10, leftMenu, infoBox);

        Label updatesTitle = new Label("Notifications & Updates");
        updatesTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        updatesArea = new TextArea();
        updatesArea.setEditable(false);
        updatesArea.setWrapText(true);
        updatesArea.setPrefWidth(800);
        updatesArea.setPrefHeight(230);

        updatesArea.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-control-inner-background: white;" +
                        "-fx-text-fill: black;"
        );

        VBox updatesSection = new VBox(10, updatesTitle, updatesArea);

        VBox root = new VBox(20, title, topSection, updatesSection);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1B211A;");

        stage.setScene(new Scene(root, 900, 600));
        stage.centerOnScreen();
        stage.show();

        updateStatus();
    }

    private Button createButton(String text) {

        Button btn = new Button(text);

        btn.setPrefWidth(190);
        btn.setPrefHeight(45);

        btn.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-background-color: #5A7863;" +
                        "-fx-border-color: white;" +
                        "-fx-border-radius: 12;" +
                        "-fx-text-fill: white;"
        );

        return btn;
    }

    private String infoTextStyle() {
        return "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;";
    }

    private void updateStatus() {

        try {
            String localIP = InetAddress.getLocalHost().getHostAddress();

            hostLabel.setText("Host/IP: " + localIP);
            portLabel.setText("Port: 5000");

            if (ServerMain.isServerRunning()) {
                statusLabel.setText("Status: Running");
            } else {
                statusLabel.setText("Status: Stopped");
            }

        } catch (Exception e) {
            hostLabel.setText("Host/IP: Unknown");
        }
    }

    public void addLog(String message) {

        Platform.runLater(() -> {

            if (updatesArea != null) {
                updatesArea.appendText(message + "\n");
            }

            updateStatus();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}