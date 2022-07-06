package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    @FXML
    public ComboBox<String> disksBox;// выбор диска
    @FXML
    public TextField pathField; // прописка пути файла
    @FXML
    public TableView<FileInfo> clientView;// поле клиента
    @FXML
    public TextField pathFieldServer;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox cloudPanel;
    @FXML
    public HBox btnPanel;
    @FXML
    public TextField clientInfo;
    @FXML
    public AnchorPane authPanel;
    @FXML
    public Button regBtn;
    public Stage stage;
    public ListView<String> serverView;
    public Network network;
    private boolean authenticated;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        clientInfo.setVisible(authenticated);
        clientInfo.setManaged(authenticated);
        cloudPanel.setVisible(authenticated);
        cloudPanel.setManaged(authenticated);
        btnPanel.setVisible(authenticated);
    }

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();

                if (message instanceof Authentification authentification) {
                    if (authentification.isFlag()) {
                        setAuthenticated(true);
                    }
                    if (!authentification.isFlag()) {
                        loginField.setText("Неверные логин/пароль");

                    }
                }

                if (message instanceof FileRequest fileRequest) {
                    Date currentDate = new Date();
                    clientInfo.setText(String.format("Здравствуйте %s. Вы зашли в облако %s", fileRequest.getName(), currentDate));
                }

                if (message instanceof ListFiles listFiles) {
                    System.out.println(listFiles);
                    Platform.runLater(() -> {
                        serverView.getItems().clear();
                        serverView.getItems().addAll(listFiles.getFiles());
                        pathFieldServer.setText(listFiles.getPath());
                    });

                } else if (message instanceof FileMessage fileMessage) {
                    Path current = Path.of(pathField.getText()).resolve(fileMessage.getName());
                    System.out.println(current+"Chatcontoller");
                    Files.write(current, fileMessage.getData());
                    Platform.runLater(() -> {
                        updateList(current.getParent());
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        }
    }

    // post init fx fields
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            // создается новый клиент, который подключается к "localhost" и c портом=8189
            setAuthenticated(false);
            network = new Network(8189);

            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        // создаем в clientView столбец с определением файл это или директория
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);
        // создаем в clientView столбец с именем файла/директории
        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(200);
        // создаем в clientView столбец с определением размера файла/директории
        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(120);

        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {

                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "DIR";
                        }
                        setText(text);

                    }
                }
            };
        });
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        TableColumn<FileInfo, String> fileDataColumn = new TableColumn<>("Дата изменения");
        fileDataColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDataColumn.setPrefWidth(120);

        clientView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDataColumn);
        clientView.getSortOrder().add(fileTypeColumn);

        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0);
        clientView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    Path path = Paths.get(pathField.getText()).resolve(clientView.getSelectionModel().getSelectedItem().getFilename());
                    if (Files.isDirectory(path)) {
                        updateList(path);
                    }
                }
            }
        });

        serverView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {

                    try {
                        String path = String.valueOf(Path.of(pathFieldServer.getText()).resolve(serverView.getSelectionModel().getSelectedItem()));
                        if (Files.isDirectory(Path.of(path))) {
                            System.out.println(path);
                            network.write(new PathInRequest(path));
                        } else {
                            Alert alert = new Alert(Alert.AlertType.WARNING, "Открывать файлы еще не умеем", ButtonType.OK);
                            alert.showAndWait();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        updateList(Paths.get("."));

    }


    public void updateList(Path path) {
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            clientView.getItems().clear();
            clientView.getItems().addAll(Files.list(path).map(FileInfo::new).toList());
            clientView.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить списко файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }


    private List<String> getFiles(String dir) {
        String[] list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }
    @FXML
    public void upload(ActionEvent actionEvent) throws IOException {
        Path path = Paths.get(pathField.getText()).resolve(clientView.getSelectionModel().getSelectedItem().getFilename());
        network.write(new ChangePathServerRequest(pathFieldServer.getText()));
        network.write(new FileMessage(path));
    }
    @FXML
    public void download(ActionEvent actionEvent) throws IOException {
        String file = (serverView.getSelectionModel().getSelectedItem());
        network.write(new ChangePathServerRequest(pathFieldServer.getText()));
        network.write(new FileRequest(file));
    }

    public void selectDiskAction(ActionEvent actionEvent) {
    }

    // нажимаем на стрелку и у нас путь поднимается на один уровень вверх
    public void btnPathUpRequest(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void btnPathUpRequestServer(ActionEvent actionEvent) {
        String path = String.valueOf(Path.of(pathFieldServer.getText()).getParent());
        try {
            network.write(new PathUpRequest(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public String getSelectedFilename() {

        return pathField.getSelectedText();
    }

    @FXML
    public void tryToAuth(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        network.write(new Authentification(login, password));
    }
    @FXML
    public void tryToReg(ActionEvent actionEvent) {
        regBtn.getScene().getWindow().hide();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("reg-view.fxml"));
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
    @FXML
    public void tryToDelete(ActionEvent actionEvent) {
        try {
            if(clientView.isFocused()) {
                Path path = Paths.get(pathField.getText()).resolve(clientView.getSelectionModel().getSelectedItem().getFilename());
                Files.delete(path);
                updateList(Paths.get(pathField.getText()));
            }else if (serverView.isFocused()) {
                Path pathServer=Path.of(pathFieldServer.getText()).resolve(serverView.getSelectionModel().getSelectedItem());
                Files.delete(pathServer);
                ListFiles listFiles=new ListFiles(Path.of(pathFieldServer.getText()));
                Platform.runLater(() -> {
                    serverView.getItems().clear();
                    serverView.getItems().addAll(listFiles.getFiles());
                    pathFieldServer.setText(listFiles.getPath());
                });
            }


        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось удалить файл", ButtonType.OK);
            alert.showAndWait();
        }
    }
}
