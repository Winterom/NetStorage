package appServer;


import lombok.extern.slf4j.Slf4j;


import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class AppServer {

    private final String ROOT_DIR_NAME = "Storage";
    private final int PORT = 8089;

    public void start(){
        File pathToRootDir = new File(new File(System.getProperty("user.dir")), ROOT_DIR_NAME);
        if(!pathToRootDir.exists()){
            pathToRootDir.mkdir();
        }
        try (ServerSocket srv = new ServerSocket(PORT)){
            log.debug("Server started...");
            while (true){
                log.debug("Server wait...");
                Socket socket = srv.accept();
                log.debug("Client connection");
                Handler handler = new Handler(socket,pathToRootDir);
                new Thread(handler).start();
            }
        }catch (IOException e){
            log.error("stacktrace ",e);
        }
    }
}
