package client;

import models.Student;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class StudentClient {

    public static void main(String[] args) {

        String serverIP = "localhost";
        int port = 5000;

        try {
            Socket socket = new Socket(serverIP, port);

            System.out.println("Connected to teacher server!");

            DataInputStream input =
                    new DataInputStream(socket.getInputStream());

            DataOutputStream output =
                    new DataOutputStream(socket.getOutputStream());

            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            Student student = new Student(username, password);

            output.writeUTF("STUDENT");
            output.writeUTF(student.getUsername());

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

            while (true) {
                String message = scanner.nextLine();
                output.writeUTF(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}