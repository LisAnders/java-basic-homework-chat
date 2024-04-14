package ru.kravchenko.homework.chat.server;

public interface AuthService {
    String getUsernameByLoginAndPassword(String login, String password);

    boolean register(String login, String password, String nickname);

    boolean isLoginAlreadyExists(String login);

    boolean isNicknameExists(String nickname);

    void grantAdmin(String nickname);

    void grantUser(String nickname);

    boolean kick(String nickname);

    boolean isAdminUser(String nickname);
}
