package app;


import at.favre.lib.crypto.bcrypt.BCrypt;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.extern.slf4j.Slf4j;
import message.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;


@Slf4j
public class ClientNet implements Runnable {

    ObjectEncoderOutputStream os;
    ObjectDecoderInputStream is;
    Socket socket;
    boolean isAuth = false;
    @Override
    public void run() {
        try {
            socket = new Socket(AppProperties.getHOST(),AppProperties.getPORT());
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            //вывести сообщение об успешном соединении с сервером
            Authentication authentication = new Authentication();
            authentication.setCommandType(CommandType.AUTH);
            authentication.setMessageType(MessageType.REQUEST);
            authentication.setLogin(AppProperties.getInstance().getLogin());
            String bcryptHashPassword = BCrypt.withDefaults().hashToString(12,
                    AppProperties.getInstance().getPassword().toCharArray());
            authentication.setHashPassword(bcryptHashPassword);
            System.out.println(bcryptHashPassword);
            os.writeObject(authentication);
            os.flush();
            Authentication response = (Authentication) is.readObject();
            if (response.getResponseCode().equals("200")){
                isAuth = true;
                //вывести сообщение об успешной аутентификации
            }

        } catch (IOException | ClassNotFoundException e) {
            log.error("stacktrace ", e);
        } finally {
            closeStream();
        }

    }

    public void sendFileToServer(FileInfo fileInfo) {
        try {
            //Перекидка из Path в String и обратно нужна потому что объект Path не сериализуемый
            //
            if (!isAuth){
                return;
            }
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
