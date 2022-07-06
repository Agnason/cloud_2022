package com.geekbrains.cloud.june.cloudapplication;


import com.geekbrains.cloud.Authentification;
import com.geekbrains.cloud.CloudMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    @FXML
    public Button tryToRegBtn;
    @FXML
    public Button tryToAuthBtn;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    private Network network;

    private Stage stage;
    private RegController regController;

    public boolean authentificated;
    public boolean noneauth;

    public ChatController chatController;

    public AuthController(ChatController chatController) {
        this.chatController = chatController;
    }

    @FXML
    void tryToReg(ActionEvent event) {
//        String login = loginField.getText().trim();
//        String password = passwordField.getText().trim();
//        network.write(new Authentification(login, password));

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("hello-view.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Parent root = loader.getRoot();
        stage = new Stage();
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // создается новый клиент, который подключается к "localhost" и c портом=8189
        // try {
        //network = new Network(8189);
        Thread readThread = new Thread(this::readLoop);
        readThread.setDaemon(true);
        readThread.start();
        authentificated = false;
        noneauth = true;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

    }


    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = chatController.network.read();

                if (message instanceof Authentification authentification) {
                    if (authentification.isFlag()) {
                        loginField.setText("Что то есть");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        }
    }


    @FXML
    public void tryToAuth(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        chatController.network.write(new Authentification(login, password));
        if (authentificated) {
            toAuth();
        } else if (!noneauth) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Логин/пароль не верны", ButtonType.OK);
            alert.showAndWait();
        }

    }

    public void toAuth() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("hello-view.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Parent root = loader.getRoot();
        stage = new Stage();
        stage.setScene(new Scene(root));

        stage.showAndWait();

    }

}

