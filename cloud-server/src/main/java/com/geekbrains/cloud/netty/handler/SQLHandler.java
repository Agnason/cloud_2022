package com.geekbrains.cloud.netty.handler;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement psGetNickname;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeNick;
    private static PreparedStatement psGetId;


    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:base.db");
            prepareAllStatements();
            System.out.println("base is created");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void prepareAllStatements() throws SQLException {
        psGetNickname = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
        psRegistration = connection.prepareStatement("INSERT INTO users (login, password, nickname) VALUES (?,?,?);");
        psChangeNick = connection.prepareStatement("UPDATE users SET nickname=? WHERE nickname=?;");
        psGetId = connection.prepareStatement("SELECT id FROM users WHERE login = ? AND password = ?;");
    }

    public static String getNicknameByLoginAndPassword(String login, String password) {
        String nick = null;
        try {
            psGetNickname.setString(1, login);
            psGetNickname.setString(2, password);
            ResultSet rs = psGetNickname.executeQuery();
            if (rs.next()) {
                nick = rs.getString(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nick;
    }
    public static Integer getIdByLoginAndPassword(String login, String password) {
        Integer id = null;
        try {
            psGetId.setString(1, login);
            psGetId.setString(2, password);
            ResultSet rs = psGetId.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static boolean registration(String login, String password, String nickname) {
        try {
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.setString(3, nickname);
            psRegistration.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean changeNick (String oldNick, String newNick) {
        try {
            psChangeNick.setString(1, newNick);
            psChangeNick.setString(2, oldNick);
            psChangeNick.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void disconnect() {
        try {
            psGetNickname.close();
            psRegistration.close();
            psChangeNick.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
