package appServer;


import appServer.handlersForClientSrv.AuthenticationHandler;
import appServer.handlersForClientSrv.SocketAccounting;
import appServer.handlersForMonitoringSrv.CommandMonitoringProcessingHandler;
import appServer.handlersForMonitoringSrv.ServiceForMonitoring;
import appServer.serviceApp.DBConnection;
import appServer.serviceApp.SrvProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.*;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class AppServer {

    private final static String ROOT_DIR_NAME = "Storage";
    Path pathToRootDir;



    //возвращаем false если что то настроить не удалось.
    public boolean init(){
        this.pathToRootDir = Paths.get((System.getProperty("user.dir")), ROOT_DIR_NAME);
        if(!Files.exists(pathToRootDir)){
            try {
                Files.createDirectory(pathToRootDir);
            } catch (IOException e) {
                log.error(e.getMessage());
                return false;
            }
        }

        if (!SrvProperties.getInstance().isGood()){
            return false;
        }
        //Запускаем liquibase
        runliquibase();
        //работаем с log4j через slf4j
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        //устанавливаем настройки log4j
        setPropertiesLog4j();

        return true;
    }

    public void start(int portForClient, int portForTelnet){
        //Сбор статистической информации
        ServiceForMonitoring service = new ServiceForMonitoring();

        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        EventLoopGroup auth2 = new NioEventLoopGroup(1);
        EventLoopGroup worker2 = new NioEventLoopGroup(1);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            ChannelFuture channelFuture = bootstrap.group(auth, worker).
                    channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new SocketAccounting(service),//сбор текущей статистики
                            new ObjectEncoder(),
                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                            new AuthenticationHandler(service)//пока кроме auth никаких хендлеров не будет
                    );
                }
            }).bind(portForClient).sync();

            log.debug("Server for client starting on : "+portForClient);

            //Создаем второй сервер для мониторинга
            //к нему можно подключиться и по telnet

            ServerBootstrap bootstrapMonitoring = new ServerBootstrap();
            ChannelFuture channelFutureMonitoring = bootstrapMonitoring.group(auth2, worker2).
                    channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new StringDecoder(),
                            new StringEncoder(),
                            new CommandMonitoringProcessingHandler(service)
                    );
                }
            }).bind(portForTelnet).sync();
            log.debug("Server for monitoring starting on : "+portForTelnet);
            //здесь текущий поток в ожидании завершения
            channelFuture.channel().closeFuture().sync();
            channelFutureMonitoring.channel().closeFuture().sync();
        }catch (InterruptedException e){
            log.error("stacktrace ",e);
        }
        finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
            auth2.shutdownGracefully();
            worker2.shutdownGracefully();

        }

    }

    private void setPropertiesLog4j() {
        ConsoleAppender console = new ConsoleAppender();
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.ALL);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);

    }

    private void runliquibase() {
        Connection c = DBConnection.getInstance().getConnection();
        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            Liquibase liquibase = new Liquibase("db/dbchangelog-master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        } catch (LiquibaseException e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.rollback();
                    c.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
