package clientGUI;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void openSetupStage(ActionEvent actionEvent) {
        try {
            AnchorPane setupPage = new FXMLLoader().load(getClass().getResource("/setupFrm.fxml"));
            Stage setupStage = new Stage();
            setupStage.initStyle(StageStyle.UTILITY);
            setupStage.setTitle("Настройки");
            setupStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(setupPage);
            setupStage.setScene(scene);
            setupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exitProgram(ActionEvent actionEvent) {
        Platform.exit();
    }
}
