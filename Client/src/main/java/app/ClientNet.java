package app;


import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;


@Slf4j
public class ClientNet implements Runnable {
    private final int PORT = 8089;
    private final byte[] buffer = new byte[1024];
    DataOutputStream os;
    DataInputStream is;
    Socket socket;

    @Override
    public void run() {
        try {
            socket = new Socket("localhost", PORT);
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());
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

    public void sendFileToServer(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        String fileName = file.getName();
        long fileSize = file.length();

        try(InputStream filestream = new FileInputStream(path)) {
            os.writeUTF(fileName);
            os.writeLong(fileSize);
            int read;
            while ((read = filestream.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        } catch (IOException e) {
            log.error("stacktrace ", e);
        }
    }

    public void closeStream() {
        try {
            os.close();
            is.close();
            socket.close();
        } catch (IOException e) {
            log.error("stacktrace: ", e);
        }
    }
}
