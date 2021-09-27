package clientGUI;

import app.AppProperties;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import java.io.IOException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

@Slf4j
public class PropertiesFrmController implements Initializable {

    @FXML
    public PasswordField password;
    @FXML
    public TextField rootDir;
    @FXML
    public CheckBox sendError;
    @FXML
    public TextField login;
    @FXML
    public Button saveButton;
    @FXML
    public Label messageLabel;


    final DirectoryChooser directoryChooser = new DirectoryChooser();



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rootDir.setText(AppProperties.getInstance().getRootDir());
        sendError.setSelected(AppProperties.getInstance().isSendStacktraceToServer());
        password.setText(AppProperties.getInstance().getPassword());
        login.setText(AppProperties.getInstance().getLogin());
    }

    public void changeDirectory(ActionEvent actionEvent) {
        File dir = directoryChooser.showDialog((Stage) saveButton.getScene().getWindow());
        if (dir != null){
            rootDir.setText(dir.getAbsolutePath());
        }
    }

    public void saveProperties(ActionEvent actionEvent) {
        Path dir = Path.of(rootDir.getText());
        if (!Files.exists(dir)){
            try {
                Files.createDirectory(dir);
            } catch (IOException e) {
                log.error("stacktrace ",e);
            }
        }
        if (AppProperties.validateDirectory(dir)){
            AppProperties.getInstance().setRootDir(rootDir.getText());
        }else {
            messageLabel.setText("Директория не существует или она не пустая");
            return;
        }
        if (AppProperties.validateLogin(login.getText())){
            AppProperties.getInstance().setLogin(login.getText());
        }else {
            messageLabel.setText("Длина логина не удовлетворяет требованиям");
            return;
        }
        if (AppProperties.validationPassword(password.getText())){
            AppProperties.getInstance().setPassword(password.getText());
        }else {
            messageLabel.setText("Длина пароля не удовлетворяет требованиям");
            return;
        }
        AppProperties.getInstance().savePropertiesFile();
    }

    public void cancel(ActionEvent actionEvent) {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
