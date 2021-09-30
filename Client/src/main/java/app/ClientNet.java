package app;


import app.handlers.AuthenticationHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.*;
import lombok.extern.slf4j.Slf4j;
import message.*;


@Slf4j
public class ClientNet {

    private static volatile ClientNet instance;

    private SocketChannel channelNet;

    private final NetCallback netCallback;

    boolean isAuth = false;

    public static ClientNet getInstance(NetCallback netCallback){
        if (instance == null){
            synchronized (ClientNet.class){
                if (instance == null){
                    instance = new ClientNet(netCallback);
                }
            }
        }
        return instance;
    }

    public ClientNet(NetCallback netCallback) {
        this.netCallback = netCallback;
        Thread thread = new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.
                        group(workerGroup).
                        channel(NioSocketChannel.class).
                        handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                channelNet = ch;
                                channelNet.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new AuthenticationHandler(isAuth, netCallback)
                                );
                            }
                        });
                ChannelFuture f = bootstrap.connect(ClientProperties.getHOST(), ClientProperties.getPORT()).sync();
                f.channel().closeFuture().sync();
            }catch (InterruptedException e){
                log.error(e.getMessage());
            }finally {
                workerGroup.shutdownGracefully();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendRequest(Command command) {
        channelNet.writeAndFlush(command);
    }
}
