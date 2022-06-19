package com.geekbrains.cloud;

public class ChangePathServerRequest implements CloudMessage {
    private String path;
    public ChangePathServerRequest(String path) {
        this.path=path;
    }

    public String getPath() {
        return path;
    }
}