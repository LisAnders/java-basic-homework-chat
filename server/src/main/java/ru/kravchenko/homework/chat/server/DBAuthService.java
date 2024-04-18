package ru.kravchenko.homework.chat.server;

import java.sql.*;

public class DBAuthService {
    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DATABASE_LOGIN = "postgres";
    private static final String DATABASE_PASSWORD = "password";
    private static Connection connection;

    public static void connect() throws SQLException {
        connection = DriverManager.getConnection(DATABASE_URL, DATABASE_LOGIN, DATABASE_PASSWORD);
    }

    public static void register(String nickname, String email, String password) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("insert into users (nickname, email, password) values (?, ?, ?)");
        ps.setString(1, nickname);
        ps.setString(2, email);
        ps.setString(3, password);
        ps.executeUpdate();
    }

    public static boolean isEmailExists(String email) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select email from users where email = ?");
        ps.setString(1, email);
        try (ResultSet resultSet = ps.executeQuery()) {
            while (resultSet.next()) {
                String resultEmail = resultSet.getString("email");
                if (resultEmail.equals(email)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isNicknameExists(String nickname) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select nickname from users where nickname = ?");
        ps.setString(1, nickname);
        try (ResultSet resultSet = ps.executeQuery()) {
            while (resultSet.next()) {
                String resultnickname = resultSet.getString("nickname");
                if (resultnickname.equals(nickname)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getNicknameByEmailAndPassword(String email, String password) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select nickname from users where email = ? and password = ?");
        ps.setString(1, email);
        ps.setString(2, password);
        try (ResultSet resultSet = ps.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString("nickname");
            }
        }
        return null;
    }

    public static boolean isAdmin(String nickname) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("""
                select lower(r.name) as name from users u
                left join user_roles ur ON u.user_id = ur.user_id
                left join roles r on r.role_id = ur.role_id
                where u.nickname = ?""");
        ps.setString(1, nickname);
        try (ResultSet resultSet = ps.executeQuery()){
            while (resultSet.next()){
                String role = resultSet.getString("name");
                if(role.equals("admin")){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isUser(String nickname) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("""
                select lower(r.name) as name from users u
                left join user_roles ur ON u.user_id = ur.user_id
                left join roles r on r.role_id = ur.role_id
                where u.nickname = ?""");
        ps.setString(1, nickname);
        try (ResultSet resultSet = ps.executeQuery()){
            while (resultSet.next()){
                String role = resultSet.getString("name");
                if(role.equals("user")){
                    return true;
                }
            }
        }
        return false;
    }

    public static void grantAdmin(String nickname) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("""
                insert into user_roles (user_id, role_id) values (
                	 (select user_id from users where nickname = ?)
                	,(select role_id from roles where name = 'ADMIN')
                )
                """);
        ps.setString(1, nickname);
        ps.executeUpdate();
    }

    public static void grantUser(String nickname) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("""
                insert into user_roles (user_id, role_id) values (
                	 (select user_id from users where nickname = ?)
                	,(select role_id from roles where name = 'USER')
                )
                """);
        ps.setString(1, nickname);
        ps.executeUpdate();
    }

}
