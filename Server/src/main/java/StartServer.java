import appServer.AppServer;


public class StartServer {
    private static final int PORT_FOR_CLIENT = 8089;
    private static final int PORT_FOR_TELNET = 8090;

    public static void main(String[] args) {
        AppServer srv = new AppServer();
        if(!srv.init()){
            System.out.println("Продолжение запуска сервера невозможно");
            return;
        }
        srv.start(StartServer.PORT_FOR_CLIENT,PORT_FOR_TELNET);
    }
}
