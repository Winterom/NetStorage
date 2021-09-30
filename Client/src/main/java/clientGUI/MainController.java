package clientGUI;

import app.ClientProperties;
import app.ClientNet;
import app.NetCallback;
import app.SynchronizeFileList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import message.Command;
import message.FileInfo;
import message.FileListRequest;
import message.FileListResponse;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {
    @FXML
    private Label messageLabel;
    @FXML
    private Button insertButton;

    @FXML
    private TableView<FileInfo> fileInfoTableView;

    @FXML
    private TextField pathField;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ClientNet.getInstance(new NetCallback() {
            @Override
            public void call(Command command) {
                switch (command.getCommandType()){
                    case  LIST_FILE_RESPONSE:
                        FileListResponse listResponse = (FileListResponse)command;
                        SynchronizeFileList synchronizeFileList = new SynchronizeFileList(listResponse.getFileList());
                        synchronizeFileList.start();
                    break;

                }

            }
        });
        if (ClientProperties.getInstance().getRootDir().isEmpty()||
                ClientProperties.getInstance().getLogin().isEmpty()||
                ClientProperties.getInstance().getPassword().isEmpty()){
            openPropertiesStage(null);
        }



        insertButton.setDisable(true);//Сначало надо что то скопировать в буфер

        //Настройка колонок таблицы
        Image dirImg = new Image(this.getClass().getResourceAsStream("/image/folder.png"));
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getName()));
        fileTypeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                        setGraphic(null);
                    } else {
                        if (item.equals(FileInfo.FileType.DIRECTORY.getName())) {
                            ImageView imageDir = new ImageView(dirImg);
                            imageDir.setFitHeight(25);
                            imageDir.setFitWidth(25);
                            setGraphic(imageDir);
                        }

                        setAlignment(Pos.CENTER);
                    }
                }
            };
        });
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameColumn.setPrefWidth(260);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%d bytes", item);
                        if (item == -1) {
                            text = "[DIR]";
                        }
                        setText(text);
                        setAlignment(Pos.CENTER);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileModifiedColumn = new TableColumn<>("Дата изменения");
        fileModifiedColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileModifiedColumn.setPrefWidth(120);

        TableColumn<FileInfo, Boolean> fileSyncColumn = new TableColumn<>("Синхронизировано");
        fileSyncColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().isFileSynchronized()));
        fileSyncColumn.setPrefWidth(176);

        fileInfoTableView.getColumns().addAll(fileTypeColumn, fileNameColumn,
                fileSizeColumn, fileModifiedColumn, fileSyncColumn);

        fileInfoTableView.getSortOrder().add(fileTypeColumn);

        //Перемещение по директориям по двойному клику мыши или отправляем файл на сервер если это файл
        fileInfoTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    Path path = Paths.get(pathField.getText()).resolve(fileInfoTableView.
                            getSelectionModel().getSelectedItem().getFileName());
                    if (Files.isDirectory(path)) {
                        updateFileList(path);
                    }
                }
            }
        });

        updateFileList(Paths.get(ClientProperties.getInstance().getRootDir()));
    }


    public void openPropertiesStage(ActionEvent actionEvent) {
        try {
            AnchorPane setupPage = new FXMLLoader().load(getClass().getResource("/propertiesFrm.fxml"));
            Stage setupStage = new Stage();
            setupStage.initStyle(StageStyle.UTILITY);
            setupStage.setTitle("Настройки");
            setupStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(setupPage);
            setupStage.setScene(scene);
            setupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnUpPathAction(ActionEvent actionEvent) {
        Path oldPath = Paths.get(pathField.getText());
        Path parentPath = Paths.get(pathField.getText()).getParent();
        if (oldPath.relativize(parentPath).toString().equals("..")){
            updateFileList(Path.of(ClientProperties.getInstance().getRootDir()));
            return;
        }
        updateFileList(parentPath);
    }

    public void updateFileList(Path path) {
        //normalize нужно что бы убрать всякие условные переходы типа ".", "../.." и т д
        pathField.setText(path.normalize().toAbsolutePath().toString());
        fileInfoTableView.getItems().clear();
        try {
            fileInfoTableView.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            fileInfoTableView.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов по указанному пути", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void copyPathToClipboard(ActionEvent actionEvent) {

    }

    public void insertFile(ActionEvent actionEvent) {

    }

    public void deleteFile(ActionEvent actionEvent) {
        if (this.fileInfoTableView.getSelectionModel().getSelectedItem() == null) {
            Alert alertNothing = new Alert(Alert.AlertType.WARNING, "Файл не выбран!", ButtonType.OK);
            alertNothing.setTitle("Ошибка!");
            alertNothing.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Вы действительно хотите удалить файл " +
                this.fileInfoTableView.getSelectionModel().getSelectedItem().getFileName() + "!", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Удаление файла");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                Files.delete(Paths.get(pathField.getText()).resolve(fileInfoTableView.getSelectionModel().getSelectedItem().getFileName()));
                messageLabel.setText("Файл " + fileInfoTableView.getSelectionModel().getSelectedItem().getFileName() + " успешно удален!");
                updateFileList(Paths.get(pathField.getText()));
            } catch (IOException e) {
                messageLabel.setText("Файл " + fileInfoTableView.getSelectionModel().getSelectedItem().getFileName() + " не удалось удалить!");
                e.printStackTrace();
            }
        }

    }

    public void synchronizedFile(ActionEvent actionEvent) {
        FileListRequest request = new FileListRequest();
        ClientNet.getInstance().sendRequest(request);

    }


    public void returnAction(ActionEvent actionEvent) {
    }
}
