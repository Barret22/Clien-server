package org.example.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 1234;

        try (Socket socket = new Socket(host, port);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println(input.readLine());

            String message;
            while (true) {
                System.out.print("Введи команду: ");
                message = scanner.nextLine();
                output.println(message);

                String response = input.readLine();
                System.out.println(response);

                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[CLIENT] Помилка: " + e.getMessage());
        }
    }
}

