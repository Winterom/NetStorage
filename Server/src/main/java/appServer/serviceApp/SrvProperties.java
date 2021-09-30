package appServer.serviceApp;

import lombok.Getter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SrvProperties {

    private static volatile SrvProperties instance;

    private final Properties prop;
    @Getter
    private String driver   = "com.mysql.jdbc.Driver";
    @Getter
    private String url      = "jdbc:mysql://localhost:";
    @Getter
    private String portDb;
    @Getter
    private String dbName;
    @Getter
    private String userName;
    @Getter
    private String password;
    @Getter
    private boolean isGood;

    public static SrvProperties getInstance(){
        if (instance == null){
            synchronized (SrvProperties.class){
                if (instance == null){
                    instance = new SrvProperties();
                }
            }
        }
        return instance;
    }

    private SrvProperties() {

        this.isGood =true;
        this.prop = new Properties();
        if (!Files.exists(Path.of("srv.properties"))) {
            saveDefaultPropertiesFile();
        }
        try (InputStream input = new FileInputStream("srv.properties")) {

            prop.load(input);
            portDb = prop.getProperty("portDb");
            userName = prop.getProperty("userName");
            password = prop.getProperty("password");
            dbName = prop.getProperty("dbName");

        }catch (IOException e){
            this.isGood =false;
            e.printStackTrace();
        }
    }

    private void saveDefaultPropertiesFile() {
        try (OutputStream fos = Files.newOutputStream(Path.of("srv.properties"));){
            prop.setProperty("portDb", "3306");
            prop.setProperty("dbName", "storage");
            prop.setProperty("userName", "winterom");
            prop.setProperty("password","London8793");
            prop.store(fos,null);
        }catch (IOException e) {
            this.isGood =false;
            e.printStackTrace();
        }
    }
}
