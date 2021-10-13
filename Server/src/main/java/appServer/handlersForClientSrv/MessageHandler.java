package appServer.handlersForClientSrv;

import appServer.serviceApp.EntityUser;
import appServer.serviceApp.SrvProperties;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import message.Command;
import message.FileMessageHeader;
import message.FileMessagePart;
import message.ResultOfSynchFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Arrays;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Command> {
   private final EntityUser user;
   private FileMessageHeader fileMessageHeader;
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
                messageHeader(fileMessageHeader,ctx);
                break;
            case FILE_MESSAGE_PART:
                FileMessagePart part = (FileMessagePart) command;
                writePartOfFile(part,ctx);
                break;
            default:
                System.out.println(command.getCommandType());break;
        }
    }

    private void messageHeader(FileMessageHeader fileMessageHeader, ChannelHandlerContext ctx) {
        Path fileName = Paths.get(fileMessageHeader.getRelativizePath()).getFileName();
        temporaryFileName = Paths.get(fileName.toString().
                replaceAll("\\.","")+".temporary");
        log.info("получили сообщение с заголовком файла "+fileMessageHeader.getRelativizePath());
        Path tempPath = SrvProperties.getInstance().getPathToRootDir().resolve(temporaryFileName);
       try {
                if(Files.exists(tempPath)){
                    Files.delete(tempPath);
                }
                Files.createFile(tempPath);//создаем временный файл
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.toString());
                ResultOfSynchFile result = new ResultOfSynchFile(405);
                result.setRelativizePath(fileMessageHeader.getRelativizePath());
                ctx.writeAndFlush(result);
            }
        }


    private void writePartOfFile(FileMessagePart part, ChannelHandlerContext ctx) {
        log.info("Получен пакет "+part.getNumberOfPart());
        Path tempPath =SrvProperties.getInstance().
                getPathToRootDir().resolve(temporaryFileName);
        //Если это первая часть файла то по идеии временного файла не должно быть
        //Но на всякий случай мы проверяем так как файл все таки может если предыдущая
        // синхронизация завершилась не удачно и мы его удаляем


        try (FileChannel fileChannel = FileChannel.open(tempPath,StandardOpenOption.APPEND,StandardOpenOption.WRITE)){
            System.out.println(Arrays.toString(part.getBuffer()));
            ByteBuffer buffer = ByteBuffer.wrap(part.getBuffer(),0, part.getCountOfData());
            fileChannel.write(buffer);
            //если кусок последний то все закрываем
            //временный файл переименовываем в нормальное имя
            if (part.getNumberOfPart()==fileMessageHeader.getQuantityParts()){
                if (!(Files.size(tempPath)==fileMessageHeader.getSize())){
                    log.error("Передать то файл передали а размер не сходится");
                    log.error("Файл результат: "+tempPath);
                    log.error("Размер результата: "+Files.size(tempPath));
                    log.error("Размер мсходного файла: "+fileMessageHeader.getSize());
                    ResultOfSynchFile result = new ResultOfSynchFile(400);
                    result.setRelativizePath(fileMessageHeader.getRelativizePath());
                    ctx.writeAndFlush(result);
                }
                Files.move(tempPath,SrvProperties.getInstance().
                        getPathToRootDir().resolve(Path.of(fileMessageHeader.getRelativizePath()).getFileName()));


                ResultOfSynchFile result = new ResultOfSynchFile(200);
                result.setRelativizePath(fileMessageHeader.getRelativizePath());
                ctx.writeAndFlush(result);
                fileMessageHeader =null;//обнуляем все для следующего файла
                temporaryFileName = null;
            }
        }catch (IOException e){
            e.printStackTrace();
            log.error(e.getMessage());
            ResultOfSynchFile result = new ResultOfSynchFile(440);
            result.setRelativizePath(fileMessageHeader.getRelativizePath());
            ctx.writeAndFlush(result);
        }

    }

}
