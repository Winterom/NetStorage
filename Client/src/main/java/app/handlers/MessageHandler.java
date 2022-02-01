package app.handlers;

import app.ClientProperties;
import clientGUI.MainController;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import message.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Command> {
    MainController mainController;

    public MessageHandler(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        switch (msg.getCommandType()) {
            case LIST_FILE_RESPONSE:
                synchronizedFile((FileListResponse) msg, ctx);
                break;
            case RESULT_OF_SYNCH_FILE:
                resultOfSynchronizedFile((ResultOfSynchFile) msg);
                mainController.updateFileList(Path.of(mainController.pathField.getText()));
                break;
            default:
                log.error("Неизвестная комманда: " + msg.toString());
                break;
        }
    }

    private void resultOfSynchronizedFile(ResultOfSynchFile msg) {
            if(msg.getCodeOfResult()==200) {
                for (FileInfo fi: mainController.fileList){
                    if (fi.getRelativizePath().equals(msg.getRelativizePath())){
                        fi.setFileSynchronized(1);
                    }
                }
            }
    }

    private void synchronizedFile(FileListResponse fileListResponse, ChannelHandlerContext ctx) {
        Platform.runLater(new Runnable() {
            public void run() {
                mainController.synchButton.setDisable(true);
                mainController.messageLabel.setText("Начался процесс синхронизации с сервером");
            }
        });

        mainController.fileList.clear();//очищаем перед заполнением
        creatListFile(Path.of(ClientProperties.getInstance().getRootDir()), mainController.fileList);
        System.out.println("после сравнения двух списков");
        List<FileInfo> result = mainController.fileList.stream().filter(fileInfo -> {
            if (fileListResponse.getFileList().containsKey(fileInfo.getRelativizePath())) {
                //Файл с таким путем на сервере есть теперь проверим дату
                LocalDateTime ldt = fileListResponse.getFileList().get(fileInfo.getRelativizePath());
                if (ldt.isAfter(fileInfo.getLastModified()) || ldt.equals(fileInfo.getLastModified())) {
                    fileInfo.setFileSynchronized(1);
                    return false;// то есть дата на серевере более свежая чем у нас
                }
            }
            return true;
        }).collect(Collectors.toList());
        result.forEach(x -> sendFileToServer(x, ctx));
        Platform.runLater(new Runnable() {
            public void run() {
                mainController.synchButton.setDisable(false);
                mainController.messageLabel.setText("Все файлы синхронизированы");
            }
        });
    }

    private void creatListFile(Path dir, Set<FileInfo> fileList) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir);) {
            files.forEach(path -> {
                if (Files.isDirectory(path)) {
                    creatListFile(path, fileList);
                } else {
                    FileInfo fi = new FileInfo(path);
                    fi.setRelativizePath(Path.of(ClientProperties.getInstance().getRootDir()).relativize(path).toString());
                    fileList.add(fi);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public void sendFileToServer(FileInfo fileInfo, ChannelHandlerContext ctx) {
        FileMessageHeader fileMessageHeader = new FileMessageHeader();
        fileMessageHeader.setSize(fileInfo.getSize());
        try {
            fileMessageHeader.setCrc32file(FileUtils.checksumCRC32(new File(fileInfo.getFullPath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileMessageHeader.setRelativizePath(fileInfo.getRelativizePath());
        System.out.println(fileMessageHeader.getRelativizePath());
        fileMessageHeader.setLastModified(fileInfo.getLastModified());
        fileMessageHeader.setQuantityParts((int) ((fileInfo.getSize() +
                ClientProperties.getBUFFER_SIZE() - 1) / ClientProperties.getBUFFER_SIZE()));
        ctx.writeAndFlush(fileMessageHeader);
        log.info("Количество посылок должно быть " + fileMessageHeader.getQuantityParts());
        try {
            FileInputStream fis = new FileInputStream(fileInfo.getFullPath());
            FileMessagePart part = new FileMessagePart();
            int i=0;
            while (true){
                i++;
                byte[] buf= new byte[ClientProperties.getBUFFER_SIZE()];
                int n = fis.read(buf);
                if (n==-1){
                    break;
                }
                part.setBuffer(buf);
                part.setCountOfData(n);
                part.setNumberOfPart(i);
                ctx.writeAndFlush(part);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
