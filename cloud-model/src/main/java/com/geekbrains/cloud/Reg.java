package com.geekbrains.cloud;

public class Reg implements CloudMessage {
    public String login;
    public String password;
    public String nick;

    public Reg(String login, String password, String nick) {
        this.login = login;
        this.password = password;
        this.nick=nick;
    }
}
