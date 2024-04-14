package ru.kravchenko.homework.chat.server;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthService implements AuthService {
    private class User {
        private String login;
        private String password;
        private String nickname;
        private UserRoles role = UserRoles.USER;

        public User(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }

        public UserRoles getRole() {
            return role;
        }

        public void setRole(UserRoles role) {
            this.role = role;
        }

        public User getUser() {
            return this;
        }
    }

    private List<User> users;

    public InMemoryAuthService() {
        this.users = new ArrayList<>();
        users.add(generateAdminUser());

    }

    private User generateAdminUser() {
        User admin = new User("admin", "password", "Admin");
        admin.setRole(UserRoles.ADMIN);
        return admin;
    }

    @Override
    public void grantAdmin(String nickname) {
        for (User u : users) {
            if (u.nickname.equals(nickname)) {
                u.setRole(UserRoles.ADMIN);
            }
        }
    }

    @Override
    public void grantUser(String nickname) {
        for (User u : users) {
            if (u.nickname.equals(nickname)) {
                u.setRole(UserRoles.USER);
            }
        }
    }

    @Override
    public boolean isAdminUser(String nickname) {
        for (User u : users) {
            if (u.nickname.equals(nickname) && u.role.equals(UserRoles.ADMIN)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean kick(String nickname) {
        for (User u : users) {
            if (u.nickname.equals(nickname)) {
                users.remove(u.getUser());
                return true;
            }
        }
        return false;
    }


    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean register(String login, String password, String nickname) {
        if (isLoginAlreadyExists(login)) {
            return false;
        }
        if (isNicknameExists(nickname)) {
            return false;
        }
        users.add(new User(login, password, nickname));
        return true;
    }

    @Override
    public boolean isLoginAlreadyExists(String login) {
        for (User u : users) {
            if (u.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNicknameExists(String nickname) {
        for (User u : users) {
            if (u.nickname.equals(nickname)) {
                return true;
            }
        }
        return false;
    }

}
