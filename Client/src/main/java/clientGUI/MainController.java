package clientGUI;

import app.ClientNet;
import app.ClientProperties;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import message.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class MainController implements Initializable {
    //Мы конечно заблокируем кнопку синхронизировать до конца синхронизации
    //с сервером но на всякий случай монитором будет объект. что бы одновременно
    // не началось две и более синхронизации
    public final Object lock = new Object();
    private ClientNet clientNet;
    @FXML
    public Button synchButton;
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
        if (ClientProperties.getInstance().getRootDir().isEmpty()||
                ClientProperties.getInstance().getLogin().isEmpty()||
                ClientProperties.getInstance().getPassword().isEmpty()){
            openPropertiesStage(null);
        }
        clientNet = new ClientNet();
        while (clientNet.getChannel()==null){

        }
        auth();
        insertButton.setDisable(true);//Сначало надо что то скопировать в буфер

        //Настройка колонок таблицы
        Image dirImg = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/folder.png")));
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
                    }else{
                        FileInfo fileInfo = fileInfoTableView.getSelectionModel().getSelectedItem();
                        fileInfo.setRelativizePath(Paths.get(ClientProperties.getInstance().getRootDir()).
                                relativize(Paths.get(fileInfo.getFullPath())).toString()
                        );
                        clientNet.sendFileToServer(fileInfo);
                        //actionForFile(fileInfoTableView.getSelectionModel().getSelectedItem().getFullPath());
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
            log.error(e.getMessage());
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
                log.error(e.getMessage());
            }
        }

    }

    public void synchronizedFile(ActionEvent actionEvent) {
        FileListRequest request = new FileListRequest();


    }


    public void returnAction(ActionEvent actionEvent) {
    }



    private void auth(){
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin(ClientProperties.getInstance().getLogin());
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(ClientProperties.getInstance().getPassword().toCharArray(),
                salt,65536,128);
        SecretKeyFactory factory = null;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            authRequest.setSalt(salt);
            authRequest.setHashPassword(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
           log.error(e.getMessage());
        }
        clientNet.sendMessage(authRequest);

    }
    //файл откроется ассоциированым в операционной системе программой
    private void actionForFile(String path){
        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            if(desktop.isSupported(Desktop.Action.OPEN)){
                try {
                    desktop.open(new File(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
