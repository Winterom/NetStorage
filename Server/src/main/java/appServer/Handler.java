package appServer;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

@Slf4j
public class Handler implements Runnable {
    File pathToRootDir;
    private final Socket socket;
    private final int BUFFER_SZE =256;
    private final byte[] buffer = new byte[BUFFER_SZE];


    public Handler(Socket socket,File pathToRootDir) {
        this.socket = socket;
        this.pathToRootDir = pathToRootDir;
    }

    @Override
    public void run() {
        try (DataInputStream is = new DataInputStream(socket.getInputStream());
             DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                String filename = is.readUTF();
                log.debug("Received filename: {}", filename);
                long filesize = is.readLong();
                log.debug("Received filesize: {}", filesize);
                int read;
                File resultPath = new File(pathToRootDir,filename);
                System.out.println(resultPath);
                try(OutputStream fileOutputStream = new FileOutputStream(resultPath)) {
                    for (int i = 0; i < (filesize + BUFFER_SZE - 1) / BUFFER_SZE; i++) {
                        read = is.read(buffer);
                        fileOutputStream.write(buffer,0,read);
                    }
                }catch (IOException e){
                    log.error("stacktrace ",e);
                }

            }
        } catch (IOException e) {
            log.error("Stacktrace ", e);
        }
    }

}
