package app;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public class ClientProperties {
    private static final int MIN_LOGIN_LENGTH =4;
    private static final int MIN_PASSWORD_LENGTH =6;
    @Getter
    private static final int BUFFER_SIZE = 32;//2 в 19 степени 524288
    @Getter
    private static final int PORT = 8089;
    @Getter
    private static final String HOST = "localhost";

    private static volatile ClientProperties instance;
    @Getter@Setter
    private String rootDir="";
    @Getter@Setter
    private boolean sendStacktraceToServer;
    @Getter@Setter
    private String login="";
    @Getter@Setter
    private String password="";

    private final Properties prop;


    public static ClientProperties getInstance(){
        if (instance == null){
            synchronized (ClientProperties.class){
                if (instance == null){
                    instance = new ClientProperties();
                }
            }
        }
        return instance;
    }


    //наверное не очень хорошо вызывать в конструкторе
    //методы которые могут кинуть исключение
    private ClientProperties() {
        this.prop = new Properties();
            if (!Files.exists(Path.of("config.properties"))) {
                savePropertiesFile();
            }
        try (InputStream input = new FileInputStream("config.properties")) {

            prop.load(input);
            rootDir = prop.getProperty("rootDir");
            login = prop.getProperty("login");
            password = prop.getProperty("password");
            sendStacktraceToServer = prop.getProperty("sendError").equals("Yes");

        }catch (IOException e){
            log.error("stacktrace ",e);
        }
    }

    public void savePropertiesFile(){
        try (OutputStream fos = Files.newOutputStream(Path.of("config.properties"))){
            prop.setProperty("rootDir", rootDir);
            prop.setProperty("login", login);
            prop.setProperty("password", password);
            if (sendStacktraceToServer){
                prop.setProperty("sendError","Yes");
            }else prop.setProperty("sendError","No");
            prop.store(fos,null);
        }catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static boolean validateDirectory(Path dir){
        try {
            return Files.exists(dir) & Files.isDirectory(dir) & (Files.list(dir).count() == 0);
        } catch (IOException e) {
           log.error(e.getMessage());
        }
        return false;
    }
    public static boolean validateLogin(String newLogin){
        return  (newLogin.length()> MIN_LOGIN_LENGTH);
    }
    public static boolean validationPassword(String newPassword){
        return  (newPassword.length()>MIN_PASSWORD_LENGTH);
    }
}
