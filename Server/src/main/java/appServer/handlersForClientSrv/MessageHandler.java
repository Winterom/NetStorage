package appServer.handlersForClientSrv;

import appServer.serviceApp.EntityUser;
import appServer.serviceApp.SrvProperties;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import message.Command;
import message.FileMessage;
import java.io.IOException;
import java.nio.channels.FileChannel;

import java.nio.file.*;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Command> {
   EntityUser user;
    public MessageHandler(EntityUser user){
        this.user = user;
        System.out.println("ура создали хэндлер");
    }


    private void fileMessageFromClient(FileMessage fileMessage){
        String  fileName = Paths.get(fileMessage.getRelativizePath()).getFileName().toString();
        String tempFileName = fileName.replaceAll("\\.", "")+".temporary";
        System.out.println("получили сообщение");
        Path tempFilePath = SrvProperties.getInstance().getPathToRootDir().resolve(Path.of(tempFileName));
        if(fileMessage.isFinal()){
            try {
                Files.move(tempFilePath,tempFilePath.resolveSibling(fileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            return;
        }
        try(FileChannel fileChannel = FileChannel.open(tempFilePath, StandardOpenOption.APPEND)) {
            fileChannel.write(fileMessage.getBuffer());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        System.out.println("ура пришло сообщение");
        switch (command.getCommandType()) {
            case LIST_FILE_REQUEST:break;
            case FILE_MESSAGE:
                System.out.println("message handler создан и пришло сообщение");
                FileMessage fileMessage = (FileMessage) command;
                fileMessageFromClient(fileMessage);
                break;
            default:
                System.out.println(command.getCommandType());break;
        }
    }
}
