package app;


import app.handlers.AuthenticationHandler;
import clientGUI.MainController;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import message.AuthRequest;
import message.Command;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;


@Slf4j
public class ClientNet {
    private Thread thread;
    @Getter
    private SocketChannel channel;

    public ClientNet(MainController mainController) {
        this.thread = new Thread(() -> {
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
                                        new AuthenticationHandler(mainController)
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
        this.channel.writeAndFlush(command);
    }
    public void stop(){
        thread.interrupt();
    }


    public void auth(){
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin(ClientProperties.getInstance().getLogin());
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(ClientProperties.getInstance().getPassword().toCharArray(),
                salt,65536,128);
        SecretKeyFactory factory = null;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            authRequest.setSalt(salt);
            authRequest.setHashPassword(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error(e.getMessage());
        }
        sendMessage(authRequest);

    }


}
