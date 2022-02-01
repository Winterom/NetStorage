package clientGUI;

import app.ClientNet;
import app.ClientProperties;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import message.FileInfo;
import message.FileListRequest;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
public class MainController implements Initializable {
    public static ClientNet clientNet;
    public static Thread watcherThread;

    public Set<FileInfo> fileList = new HashSet<>(); //Перечень всех файлов и директорий в хранилище
    WatchService watchService;
    WatchKey watchKey;

    @FXML
    public Button returnButton;

    private Path previuosPath = null;
    @FXML
    public Button synchButton;
    @FXML
    public Label messageLabel;

    @FXML
    private TableView<FileInfo> fileInfoTableView;

    @FXML
    public TextField pathField;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //грузим настройки из проперти файла
        if (ClientProperties.getInstance().getRootDir().isEmpty() ||
                ClientProperties.getInstance().getLogin().isEmpty() ||
                ClientProperties.getInstance().getPassword().isEmpty()) {
            openPropertiesStage(null);
        }
        //создаем экземляр объекта сетевого клиента
        clientNet = new ClientNet(this);
        while (clientNet.getChannel() == null) {
            //ждем соединения с сервером
        }
        //авторизуемся
        clientNet.auth();

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Настройка колонок таблицы
        Image dirImg = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/folder.png")));
        //картинки синхронизировано или нет
        Image synchFalse = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/synchfalse.png")));
        Image synchTrue = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/synchtrue.png")));
        //картинки по расширению файла
        Image aviFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/avi.png")));
        Image bmpFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/bmp.png")));
        Image docFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/doc.png")));
        Image gifFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/gif.png")));
        Image htmlFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/html.png")));
        Image jpegFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/jpeg.png")));
        Image jsFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/js.png")));
        Image mp3File = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/mp3.png")));
        Image otherFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/other.png")));
        Image pdfFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/pdf.png")));
        Image pngFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/png.png")));
        Image pptFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/ppt.png")));
        Image psdFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/psd.png")));
        Image xlsFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/xls.png")));
        Image zipFile = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/image/fileType/zip.png")));


        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().toString()));
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
                        ImageView imageFileType;
                        switch (FileInfo.FileType.valueOf(item)) {
                            case DIRECTORY:
                                imageFileType = new ImageView(dirImg);
                                break;
                            case JS:
                                imageFileType = new ImageView(jsFile);
                                break;
                            case AVI:
                                imageFileType = new ImageView(aviFile);
                                break;
                            case BMP:
                                imageFileType = new ImageView(bmpFile);
                                break;
                            case GIF:
                                imageFileType = new ImageView(gifFile);
                                break;
                            case MP3:
                                imageFileType = new ImageView(mp3File);
                                break;
                            case PDF:
                                imageFileType = new ImageView(pdfFile);
                                break;
                            case PNG:
                                imageFileType = new ImageView(pngFile);
                                break;
                            case PPT:
                                imageFileType = new ImageView(pptFile);
                                break;
                            case PSD:
                                imageFileType = new ImageView(psdFile);
                                break;
                            case ZIP:
                                imageFileType = new ImageView(zipFile);
                                break;
                            case HTML:
                                imageFileType = new ImageView(htmlFile);
                                break;
                            case JPEG:
                                imageFileType = new ImageView(jpegFile);
                                break;
                            case WORD:
                                imageFileType = new ImageView(docFile);
                                break;
                            case EXCEl:
                                imageFileType = new ImageView(xlsFile);
                                break;
                            default:
                                imageFileType = new ImageView(otherFile);
                                break;
                        }
                        imageFileType.setFitHeight(25);
                        imageFileType.setFitWidth(25);
                        setGraphic(imageFileType);
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
                        String text;
                        if (item == -1) {
                            text = "[DIR]";
                        } else {
                            text = FileUtils.byteCountToDisplaySize(item);
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

        TableColumn<FileInfo, Integer> fileSyncColumn = new TableColumn<>("Синхронизировано");
        fileSyncColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(getSynchronized(param.getValue())));
        fileSyncColumn.setCellFactory(column -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if ((item == null || empty)||(item==2)) {
                        setText(null);
                        setStyle("");
                        setGraphic(null);
                    } else {
                        ImageView imageSynch;
                        if (item == 1) {
                            imageSynch = new ImageView(synchTrue);

                        } else {
                            imageSynch = new ImageView(synchFalse);
                        }
                        setAlignment(Pos.CENTER);
                        imageSynch.setFitHeight(25);
                        imageSynch.setFitWidth(25);
                        setGraphic(imageSynch);
                    }
                }
            };
        });
        fileSyncColumn.setPrefWidth(176);

        fileInfoTableView.getColumns().addAll(fileTypeColumn, fileNameColumn,
                fileSizeColumn, fileModifiedColumn, fileSyncColumn);

        fileInfoTableView.getSortOrder().add(fileTypeColumn);
        //вешаем листенер на изменение текущего пути
        pathField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {

                try {
                    watchKey=Path.of(newValue).register(watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                    updateFileList(Path.of(newValue));
                } catch (IOException e) {
                    log.error(e.toString());
                }
            }
        });
        //реализуем drag on drop
        fileInfoTableView.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != fileInfoTableView
                        && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    //updateFileList(Path.of(pathField.getText()));
                }
                event.consume();
            }
        });
        fileInfoTableView.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    copyFilesAndDirectories(db.getFiles());
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
        //Перемещение по директориям по двойному клику мыши или отправляем файл на сервер если это файл
        fileInfoTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    if (fileInfoTableView.
                            getSelectionModel().getSelectedItem()==null){
                        return;
                    }
                    Path path = Paths.get(pathField.getText()).resolve(fileInfoTableView.
                            getSelectionModel().getSelectedItem().getFileName());
                    if (Files.isDirectory(path)) {
                        previuosPath = Path.of(pathField.getText());
                        //updateFileList(path);
                        pathField.setText(path.normalize().toAbsolutePath().toString());
                    } else {
                        actionForFile(fileInfoTableView.getSelectionModel().getSelectedItem().getFullPath());

                    }
                }
            }
        });
        pathField.setText(ClientProperties.getInstance().getRootDir());
        watcherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if ((watchKey = watchService.take()) == null) break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (WatchEvent<?> event : watchKey.pollEvents()) {
                        updateFileList(Path.of(pathField.getText()));
                    }
                    watchKey.reset();
                }
            }
        });
        watcherThread.setDaemon(true);
        watcherThread.start();
    }
    //копирует файлы с директориями и поддиректориями в отдельном потоке
    private void copyFilesAndDirectories(List<File> files) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            File newPathDir =new File(pathField.getText()+File.separator+f.getName());
                            FileUtils.copyDirectory(f,newPathDir);
                        } else {
                            FileUtils.copyFileToDirectory(f, new File(pathField.getText()));
                        }
                    }
                    //updateFileList(Path.of(pathField.getText()));
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("error " + e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    //проверяем синхронизирован ли файл
    private Integer getSynchronized(FileInfo fileInfo) {
        fileInfo.setRelativizePath(Path.of(ClientProperties.getInstance().getRootDir()).relativize(Path.of(fileInfo.getFullPath())).toString());
        if (fileList.contains(fileInfo)) {
            return fileList.stream().filter(x -> x.equals(fileInfo)).findFirst().get().getFileSynchronized();
        }
        return 2;
    }

    //открываем окно с настройками
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
    //перемещаемся в родительскую директорию
    public void btnUpPathAction(ActionEvent actionEvent) {
        Path oldPath = Path.of(pathField.getText());
        Path parentPath = Path.of(pathField.getText()).getParent();
        if (oldPath.equals(Path.of(ClientProperties.getInstance().getRootDir()))) {
            return;
        }
        //updateFileList(parentPath);
        pathField.setText(parentPath.normalize().toAbsolutePath().toString());
    }
    //обновляем текущий каталог
    public void updateFileList(Path path) {
        fileInfoTableView.getItems().clear();
        try {

            fileInfoTableView.getItems().addAll(Files.list(path).map(new Function<Path, FileInfo>() {
                @Override
                public FileInfo apply(Path path) {
                   FileInfo fileInfo = new FileInfo(path);

                   String relPath = (Path.of(ClientProperties.getInstance().getRootDir()).relativize(path)).normalize().toString();
                    if (Files.isDirectory(path)) {
                        fileInfo.setFileSynchronized(2);
                    }else {
                        fileInfo.setRelativizePath(relPath);
                        fileInfo.setFileSynchronized(getSynchronized(fileInfo));
                    }
                   return fileInfo;
                }
            }).collect(Collectors.toList()));
            fileInfoTableView.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов по указанному пути", ButtonType.OK);
            alert.showAndWait();
            log.error(e.getMessage());
        }
    }
    //копируем путь файла в системный буфер обмена
    public void copyPathToClipboard(ActionEvent actionEvent) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        ArrayList<File> files = new ArrayList<>(1);
        if (fileInfoTableView.getSelectionModel().getSelectedItem()==null){
            return;
        }
        files.add(new File(fileInfoTableView.getSelectionModel().getSelectedItem().getFullPath()));
        content.putFiles(files);
        clipboard.setContent(content);
    }

    //Вставляем файлы и директории используя системеый буфер обмена
    public void insertFile(ActionEvent actionEvent) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasFiles()) {
            return;
        }
        copyFilesAndDirectories(clipboard.getFiles());
    }

    //удаляем файл
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
                Path tp = Paths.get(pathField.getText()).resolve(fileInfoTableView.getSelectionModel().getSelectedItem().getFileName());
                if (Files.isDirectory(tp)){
                    FileUtils.deleteDirectory(tp.toFile());
                }else{
                    Files.delete(tp);
                }
                messageLabel.setText("Файл или директория " + fileInfoTableView.getSelectionModel().getSelectedItem().getFileName() + " успешно удален!");
                //updateFileList(Paths.get(pathField.getText()));
            } catch (IOException e) {
                messageLabel.setText("Файл " + fileInfoTableView.getSelectionModel().getSelectedItem().getFileName() + " не удалось удалить!");
                log.error(e.getMessage());
            }
        }
    }

    //Запрашиваем перечень файлов
    public void synchronizedFile(ActionEvent actionEvent) {
        this.synchButton.setDisable(true);
        clientNet.sendMessage(new FileListRequest());
    }

    //Возврат назад
    public void returnAction(ActionEvent actionEvent) {
        if (previuosPath == null) {
            return;
        }
        //updateFileList(previuosPath);
        pathField.setText(previuosPath.normalize().toAbsolutePath().toString());
    }


    //файл откроется ассоциированым в операционной системе программой
    private void actionForFile(String path) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    desktop.open(new File(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
