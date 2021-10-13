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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


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
    public void sendFileToServer( FileInfo fileInfo) {
        FileMessageHeader fileMessageHeader = new FileMessageHeader();
        fileMessageHeader.setSize(fileInfo.getSize());
        fileMessageHeader.setRelativizePath(fileInfo.getRelativizePath());
        System.out.println(fileMessageHeader.getRelativizePath());
        fileMessageHeader.setLastModified(fileInfo.getLastModified());
        fileMessageHeader.setQuantityParts((int) ((fileInfo.getSize() +
                ClientProperties.getBUFFER_SIZE() - 1) / ClientProperties.getBUFFER_SIZE()));
        sendMessage(fileMessageHeader);
        log.info("Количество посылок должно быть " + fileMessageHeader.getQuantityParts());
        ByteBuffer dst = ByteBuffer.allocate(ClientProperties.getBUFFER_SIZE());
        //Операция блокирующая поэтому потом поместим в отдельный поток
        int count =0;


        try (FileChannel fc = FileChannel.open(Path.of(fileInfo.getFullPath()), StandardOpenOption.READ)){
            while (true){
                int n = fc.read(dst);
                if (!(n>0)){
                    break;
                }
                dst.flip();
                FileMessagePart part = new FileMessagePart();
                System.out.println(Charset.defaultCharset().decode(dst));
                count++;
                part.setCountOfData(n);
                part.setBuffer(dst.array());
                part.setNumberOfPart(count);
                channel.writeAndFlush(part);
                dst.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
