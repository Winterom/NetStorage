package app;


import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.extern.slf4j.Slf4j;
import message.FileInfo;
import message.FileMessage;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;


@Slf4j
public class ClientNet implements Runnable {

    ObjectEncoderOutputStream os;
    ObjectDecoderInputStream is;
    Socket socket;

    @Override
    public void run() {
        try {
            socket = new Socket(AppProperties.getHOST(),AppProperties.getPORT());
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            while (true) {
                String msg = is.readUTF();
                log.debug("received from server: " + msg);
            }

        } catch (IOException e) {
            log.error("stacktrace ", e);
        } finally {
            closeStream();
        }

    }

    public void sendFileToServer(FileInfo fileInfo) {
        try {
            //Перекидка из Path в String и обратно нужна потому что объект Path не сериализуемый
            //
            Path tempPath = Path.of(AppProperties.getInstance().getRootDir());
            fileInfo.setRelativizePath(tempPath.relativize(Path.of(fileInfo.getFullPath())).toString());
            os.writeObject(new FileMessage(fileInfo));
            os.flush();
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    public void closeStream() {
        try {
            if (os !=null){
                os.close();
            }
            if (is !=null){
                is.close();
            }
            if (socket !=null){
                socket.close();
            }
        } catch (IOException e) {
            log.error(e.toString());
        }
    }
}
