package com.geekbrains.cloud;

public class Authentification implements CloudMessage{

    public String login;
    public String password;

    private boolean flag;


    public Authentification(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Authentification(boolean flag) {
        this.flag = flag;
    }

    public boolean isFlag() {
        return flag;
    }
}
