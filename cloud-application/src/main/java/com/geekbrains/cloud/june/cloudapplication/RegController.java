package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class RegController implements Initializable {

    @FXML
    public TextField loginReg;
    @FXML
    private TextField passwordReg;
    @FXML
    public TextField nickReg;

    private Network network;
    private Stage stage;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
        network = new Network(8189);
        Thread readThread = new Thread(this::readLoop);
        readThread.setDaemon(true);
        readThread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();

                if (message instanceof Authentification authentification) {
                    if (authentification.isFlag()){

                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        }
    }


    public void tryToAuth(ActionEvent actionEvent) throws IOException {

        String login = loginReg.getText().trim();
        String password = passwordReg.getText().trim();
        String nick = nickReg.getText().trim();
        network.write(new Reg(login, password, nick));
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
