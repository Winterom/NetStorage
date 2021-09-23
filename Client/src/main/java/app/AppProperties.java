package app;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public class AppProperties {
    private static volatile AppProperties instance;
    @Getter
    private Path rootDir;
    @Getter
    private String login;
    @Getter
    private String password;


    public static AppProperties getInstance(){
        if (instance == null){
            synchronized (AppProperties.class){
                if (instance == null){
                    instance = new AppProperties();
                }
            }
        }
        return instance;
    }


    //наверное не очень хорошо вызывать в конструкторе
    //методы которые могут кинуть исключение
    private AppProperties(){
        try {
            if(!Files.exists(Path.of("config.properties"))){
                Files.createFile(Path.of("config.properties"));

            }
        }catch (IOException e){
            log.error("stacktrace ",e);
        }

        try (InputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            rootDir = Path.of(prop.getProperty("rootDir"));
            login = prop.getProperty("login");
            password = prop.getProperty("password");
        }catch (IOException e){
            log.error("stacktrace ",e);
        }
    }
}
