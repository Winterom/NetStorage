package appServer;

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
    private String driver   = "com.mysql.jdbc.Driver";
    private String url      = "jdbc:mysql://localhost:";
    private String portDb   = "3306/";
    private String dbName;
    private String userName;
    private String password;

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
        this.prop = new Properties();
        if (!Files.exists(Path.of("config.properties"))) {
            savePropertiesFile();
        }
        try (InputStream input = new FileInputStream("config.properties")) {

            prop.load(input);
            portDb = prop.getProperty("portDb");
            userName = prop.getProperty("userName");
            password = prop.getProperty("password");
            dbName = prop.getProperty("dbName");

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void savePropertiesFile() {
        try (OutputStream fos = Files.newOutputStream(Path.of("config.properties"));){
            prop.setProperty("portDb", portDb);
            prop.setProperty("dbName", dbName);
            prop.setProperty("userName", userName);
            prop.setProperty("password", password);

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
