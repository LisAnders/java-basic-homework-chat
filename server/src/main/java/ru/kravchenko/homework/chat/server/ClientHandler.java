package ru.kravchenko.homework.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private boolean active = true;

    public String getNickname() {
        return nickname;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive() {
        return active;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                if (tryToAuth()) {
                    communicate();

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    private boolean tryToAuth() throws IOException {
        while (true) {
            // /auth login pass
            String msg = in.readUTF();
            if (msg.startsWith("/auth ")) {
                String[] tokens = msg.split(" ");
                if (tokens.length != 3) {
                    System.out.println("Некорректный формат запроса");
                    continue;
                }
                String login = tokens[1];
                String password = tokens[2];
                String nickname = server.getAuthService().getUsernameByLoginAndPassword(login, password);
                if (nickname == null) {
                    sendMessage("Некорректный логин или пароль");
                    continue;
                }
                if (server.isNicknameBusy(nickname)) {
                    sendMessage("Уже выполнен вход под указанной учетной записью");
                    continue;
                }
                this.nickname = nickname;
                server.subscribe(this);
                sendMessage("Добро пожаловать на чат-сервер " + this.nickname);
                return true;
            } else if (msg.startsWith("/register ")) {
                String[] tokens = msg.split(" ");
                if (tokens.length != 4) {
                    sendMessage("Некорректный формат запроса");
                    continue;
                }
                String login = tokens[1];
                String password = tokens[2];
                String nickname = tokens[3];
                if (server.getAuthService().isLoginAlreadyExists(login)) {
                    sendMessage("Указанный логин уже занят");
                    continue;
                }
                if (server.getAuthService().isNicknameExists(nickname)) {
                    sendMessage("Указанный никнейм уже занят");
                    continue;
                }
                if (!server.getAuthService().register(login, password, nickname)) {
                    sendMessage("Не удалось пройти регистрацию");
                    continue;
                }
                this.nickname = nickname;
                server.subscribe(this);
                sendMessage("Вы спешно зарегистрировались, добро пожаловать на чат-сервер " + nickname);
                return true;
            } else if (msg.startsWith("/exit")) {
                return false;
            } else {
                sendMessage("Необходимо авторизоваться");
            }
        }
    }

    private void communicate() throws IOException {
        while (true) {
            String msg = in.readUTF();
            if (this.active) {
                if (msg.startsWith("/")) {
                    if (msg.startsWith("/exit")) {
                        // disconnect();
                        break;
                    }
                    if (msg.startsWith("/w ")) {
                        String[] tokens = msg.split(" ", 3);
                        String recipient = tokens[1];
                        String message = tokens[2];
                        server.sendPrivateMessage(recipient, nickname + ": " + message);
                    }
                    if (msg.startsWith("/kick ")) {
                        if (server.getAuthService().isAdminUser(this.nickname)) {
                            String[] tokens = msg.split(" ", 3);
                            String recipient = tokens[1];
                            String message;
                            if (recipient.equals(this.nickname)) {
                                sendMessage("Нельзя выгнать самого себя");
                                continue;
                            }
                            if (!server.getAuthService().isNicknameExists(recipient)) {
                                sendMessage("Нет пользователя с ником: " + recipient);
                                continue;
                            }
                            if (tokens.length == 3) {
                                message = "с сообщением : " + tokens[2];
                            } else {
                                message = "без пояснения причин";
                            }
                            if (server.getAuthService().kick(recipient)) {
                                sendMessage("Пользователь " + recipient + " выгнан из чата");
                                server.sendPrivateMessage(recipient, "Пользователь " + this.nickname + " выгнал вас из чата " + message);
                                server.unsubscribe(server.getClientHandlerByNickname(recipient));
                            }

                        } else {
                            sendMessage("Для использования данной команды нужны права администратора");
                        }
                    }
                    if (msg.startsWith("/grant ")) {
                        if (server.getAuthService().isAdminUser(this.nickname)) {
                            String[] tokens = msg.split(" ", 3);
                            String recipient = tokens[1];
                            String role = tokens[2].toLowerCase();
                            if (!server.getAuthService().isNicknameExists(recipient)) {
                                sendMessage("Нет пользователя с ником: " + recipient);
                                continue;
                            }
                            if (role.equals("admin")) {
                                if (recipient.equals(this.nickname)) {
                                    sendMessage("У вас уже имеется данная роль");
                                    continue;
                                }
                                server.getAuthService().grantAdmin(recipient);

                            } else if (role.equals("user")) {
                                if (recipient.equals(this.nickname)) {
                                    sendMessage("Нельзя понизить роль у самого себя");
                                    continue;
                                }
                                server.getAuthService().grantUser(recipient);
                            } else {
                                sendMessage("Роль: " + role + " не заведена в системе");
                                continue;
                            }
                            server.broadcastMessage("Пользователь " + this.nickname + " выдал роль " + role + " пользователю " + recipient);
                        } else {
                            sendMessage("Для использования данной команды нужны права администратора");
                        }
                    }
                    continue;
                }
                server.broadcastMessage(nickname + ": " + msg);
            } else {
                sendMessage("У Вас нет доступа к данному чату, зарегистируйтесь");
            }
        }

    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
