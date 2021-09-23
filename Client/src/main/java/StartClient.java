import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class StartClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/mainFrm.fxml"));
        primaryStage.setTitle("Мое облачное хранилище");
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.getIcons().add(new Image("favicon.png"));
        primaryStage.setScene(new Scene(root, 700, 600));
        primaryStage.setOnCloseRequest(this::closeProgram);
        primaryStage.show();
    }

    private void closeProgram(Event event){
        //Нужно перехватить событие закрытия окна программы
    }


    public static void main(String[] args) {
        launch(args);
    }
}