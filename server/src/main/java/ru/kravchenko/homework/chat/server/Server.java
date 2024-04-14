package ru.kravchenko.homework.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.authService = new InMemoryAuthService();
            System.out.println("Cервис аутентификации запущен: " + authService.getClass().getSimpleName());
            System.out.printf("Сервер запущен на порту: %d, ожидаем клиентов\n", port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new ClientHandler(this, socket);

                } catch (Exception e) {
                    System.out.println("Ошибка при обработке подключившегося клиента");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        broadcastMessage("К чату присоеденился " + clientHandler.getNickname());
        clientHandler.setActive(true);
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        if (clientHandler.getActive()) {
            broadcastMessage(clientHandler.getNickname() + " покинул чат");
            clientHandler.setActive(false);
        }
    }

    public synchronized ClientHandler getClientHandlerByNickname(String nickname) {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(nickname)) {
                return client;
            }
        }
        return null;
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized void sendPrivateMessage(String recipient, String message) {
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(recipient)) {
                c.sendMessage(message);
            }
        }
    }

    public synchronized boolean isNicknameBusy(String nickname) {
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }
}
