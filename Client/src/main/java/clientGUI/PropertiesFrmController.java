package clientGUI;

import app.ClientProperties;
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
        rootDir.setText(ClientProperties.getInstance().getRootDir());
        sendError.setSelected(ClientProperties.getInstance().isSendStacktraceToServer());
        password.setText(ClientProperties.getInstance().getPassword());
        login.setText(ClientProperties.getInstance().getLogin());
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
        if (ClientProperties.validateDirectory(dir)){
            ClientProperties.getInstance().setRootDir(rootDir.getText());
        }else {
            messageLabel.setText("Директория не существует или она не пустая");
            return;
        }
        if (ClientProperties.validateLogin(login.getText())){
            ClientProperties.getInstance().setLogin(login.getText());
        }else {
            messageLabel.setText("Длина логина не удовлетворяет требованиям");
            return;
        }
        if (ClientProperties.validationPassword(password.getText())){
            ClientProperties.getInstance().setPassword(password.getText());
        }else {
            messageLabel.setText("Длина пароля не удовлетворяет требованиям");
            return;
        }
        ClientProperties.getInstance().savePropertiesFile();
    }

    public void cancel(ActionEvent actionEvent) {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
