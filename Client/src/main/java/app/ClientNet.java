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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import message.*;


@Slf4j
public class ClientNet {
    @Getter
    private SocketChannel channel;

    public ClientNet() {
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
                                channel = ch;
                                ch.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new AuthenticationHandler()
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

    public void sendMessage(Command command) {
        System.out.println(command);
        this.channel.writeAndFlush(command);
    }


}
