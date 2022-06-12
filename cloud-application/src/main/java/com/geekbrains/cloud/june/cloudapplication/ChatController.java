package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.CloudMessage;
import com.geekbrains.cloud.FileMessage;
import com.geekbrains.cloud.FileRequest;
import com.geekbrains.cloud.ListFiles;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {


    @FXML
    public ComboBox<String> disksBox;// выбор диска
    @FXML
    public TextField pathField; // прописка пути файла
    @FXML
    public TableView<FileInfo> clientView;// поле клиента


    private String homeDir;


   // public ListView<String> clientView;

    @FXML
    //TableView<String> serverView;
    public ListView<String> serverView;

    private Network network;

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();
                if (message instanceof ListFiles listFiles) {
                    Platform.runLater(() -> {
                        serverView.getItems().clear();
                        serverView.getItems().addAll(listFiles.getFiles());
                    });
                } else if (message instanceof FileMessage fileMessage) {
                    Path current = Path.of(pathField.getText()).resolve(fileMessage.getName());
                    System.out.println(current);
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
// кликаем мышью и входим наверх
        clientView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount()==2) {
                    Path path = Paths.get(pathField.getText()).resolve(clientView.getSelectionModel().getSelectedItem().getFilename());
                    if(Files.isDirectory(path)){
                        updateList(path);
                    }
                }
            }
        });

        updateList(Paths.get("."));
        try {
            // создается новый клиент, который подключается к "localhost" и c портом=8189
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

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

//    public void upload(ActionEvent actionEvent) throws IOException {
//        String file = (clientView.getSelectionModel().getSelectedItem().getFilename());
//        System.out.println(file);
//        network.write(new FileMessage(Path.of("C:\\Users\\anasonov\\IdeaProjects\\Cloud\\geek-cloud-2022-june-master\\server_files").resolve(file)));
//    }

    public void upload(ActionEvent actionEvent) throws IOException {
        Path path = Paths.get(pathField.getText()).resolve(clientView.getSelectionModel().getSelectedItem().getFilename());


        network.write(new FileMessage(Path.of("server_files").resolve(path)));
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String file = (serverView.getSelectionModel().getSelectedItem());
        network.write(new FileRequest(file));
    }

    public void selectDiskAction(ActionEvent actionEvent) {
    }
// нажимаем на стрелку и у нас путь поднимается на один уровень вверх
    public void btnPathUpRequest(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath !=null) {
            updateList(upperPath);
        }
    }

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public String getSelectedFilename() {

        return pathField.getSelectedText();
    }
}