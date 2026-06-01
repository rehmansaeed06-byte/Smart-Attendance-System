package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TeacherClient {

    public static void main(String[] args) {

        String serverIP = "localhost";
        int port = 5000;

        try {
            Socket socket = new Socket(serverIP, port);

            System.out.println("Connected to cloud/local server as Teacher.");

            DataInputStream input =
                    new DataInputStream(socket.getInputStream());

            DataOutputStream output =
                    new DataOutputStream(socket.getOutputStream());

            Scanner scanner = new Scanner(System.in);

            // Tell server this client is teacher
            output.writeUTF("TEACHER");

            Thread receiver = new Thread(() -> {
                try {
                    while (true) {
                        String serverMessage = input.readUTF();
                        System.out.println(serverMessage);
                    }
                } catch (Exception e) {
                    System.out.println("Disconnected from server.");
                }
            });

            receiver.start();

            System.out.println("Teacher Commands:");
            System.out.println("/list = show connected students");
            System.out.println("/send message = send message to students");
            System.out.println("/start = start lecture");
            System.out.println("/report = show attendance report");

            while (true) {
                String command = scanner.nextLine();
                output.writeUTF(command);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}