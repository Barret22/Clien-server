package org.example.server;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class ServerApp {

    private static int clientCount = 0;
    static final Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = 1234;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[SERVER] Сервер запущено на порті " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientCount++;
                String clientName = "client-" + clientCount;
                String connectedAt = new SimpleDateFormat("HH:mm:ss").format(new Date());

                ClientHandler handler = new ClientHandler(clientName, clientSocket, connectedAt);
                activeClients.put(clientName, handler);

                System.out.println("[SERVER] " + clientName + " успішно підключився о " + connectedAt);

                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Помилка: " + e.getMessage());
        }
    }

    static void removeClient(String name) {
        activeClients.remove(name);
        System.out.println("[SERVER] " + name + " відключився та видалений з активних з'єднань.");
    }

    static class ClientHandler implements Runnable {
        private final String name;
        private final Socket socket;
        private final String connectedAt;

        public ClientHandler(String name, Socket socket, String connectedAt) {
            this.name = name;
            this.socket = socket;
            this.connectedAt = connectedAt;
        }

        @Override
        public void run() {
            try (
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true)
            ) {
                output.println("[SERVER] Привіт, " + name + "! Введи 'exit' для виходу або '@client-X повідомлення' для приватного чату.");

                String message;
                while ((message = input.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        output.println("[SERVER] До побачення, " + name + "!");
                        break;
                    } else if (message.startsWith("@")) {
                        handlePrivateMessage(message, output);
                    } else {
                        output.println("[SERVER] Ви написали: " + message);
                    }
                }

            } catch (IOException e) {
                System.err.println("[SERVER] Клієнт " + name + " відключився з помилкою: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
                ServerApp.removeClient(name);
            }
        }

        private void handlePrivateMessage(String message, PrintWriter output) {
            int spaceIndex = message.indexOf(' ');
            if (spaceIndex == -1) {
                output.println("[SERVER] Неправильний формат. Використай: @client-2 Привіт");
                return;
            }

            String targetName = message.substring(1, spaceIndex);
            String privateMessage = message.substring(spaceIndex + 1);

            ClientHandler targetClient = ServerApp.activeClients.get(targetName);
            if (targetClient != null) {
                targetClient.sendMessage("[Приватне повідомлення від " + name + "]: " + privateMessage);
                output.println("[SERVER] Приватне повідомлення надіслано до " + targetName);
            } else {
                output.println("[SERVER] Клієнт " + targetName + " не знайдений.");
            }
        }

        private void sendMessage(String msg) {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(msg);
            } catch (IOException e) {
                System.err.println("[SERVER] Помилка надсилання повідомлення клієнту " + name);
            }
        }
    }
}
