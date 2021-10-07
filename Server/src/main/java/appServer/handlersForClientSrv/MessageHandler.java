package appServer.handlersForClientSrv;

import appServer.serviceApp.EntityUser;
import appServer.serviceApp.SrvProperties;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import message.Command;
import message.FileMessageHeader;
import message.FileMessagePart;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Command> {
   private final EntityUser user;
   private Path fileName;
   private FileMessageHeader fileMessageHeader;
   FileChannel fileChannel;
   Path temporaryFileName;

   public MessageHandler(EntityUser user){
        this.user = user;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        switch (command.getCommandType()) {
            case LIST_FILE_REQUEST:break;
            case FILE_MESSAGE_HEADER:
                this.fileMessageHeader= (FileMessageHeader) command;
                fileName = Paths.get(fileMessageHeader.getRelativizePath()).getFileName();
                temporaryFileName = Paths.get(fileName.toString().
                        replaceAll("\\.","")+".temporary");
                log.info("получили сообщение с заголовком файла "+fileMessageHeader.getRelativizePath());
                try (FileChannel fc = FileChannel.open(SrvProperties.getInstance().
                        getPathToRootDir().resolve(temporaryFileName))){
                    fileChannel = fc;
                }catch (IOException e){
                    log.error(e.getMessage());
                }
                break;
            case FILE_MESSAGE_PART:
                FileMessagePart part = (FileMessagePart) command;
                writePartOfFile(part);
            default:
                System.out.println(command.getCommandType());break;
        }
    }

    private void writePartOfFile(FileMessagePart part) {
        log.info("Получен пакет "+part.getNumberOfPart());
        if(fileChannel==null){
            log.error("Получен пакет в то время как канал закрыт");
            return;
        }
        try {
            fileChannel.write(ByteBuffer.wrap(part.getBuffer()));
            if (part.getNumberOfPart()==fileMessageHeader.getQuantityParts()){

                fileChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}
