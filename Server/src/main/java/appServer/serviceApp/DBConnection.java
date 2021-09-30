package appServer.serviceApp;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static volatile DBConnection instance;

    private final String urlConnection;
    private final String userName;
    private final String password;

    public static DBConnection getInstance(){
        if (instance == null){
            synchronized (DBConnection.class){
                if (instance == null){
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    private DBConnection() {
        urlConnection = SrvProperties.getInstance().getUrl()+
                SrvProperties.getInstance().getPortDb()+"/"+
        SrvProperties.getInstance().getDbName();
        userName = SrvProperties.getInstance().getUserName();
        password = SrvProperties.getInstance().getPassword();

    }

    public Connection getConnection(){
        try {
            return DriverManager.getConnection(urlConnection,userName,password);
        } catch (SQLException e) {
           e.printStackTrace();
        }
        return null;
    }

    public boolean test(){
        try {
           Connection connection=DriverManager.getConnection(urlConnection,userName,password);
            System.out.println("Соединение с базой данных прошло успешно");
            connection.close();
           return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
