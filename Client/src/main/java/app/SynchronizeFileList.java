package app;

import javafx.fxml.Initializable;
import lombok.extern.slf4j.Slf4j;
import message.FileInfo;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class SynchronizeFileList {
    List<FileInfo> listFromServer;

    public SynchronizeFileList(List<FileInfo> listFromServer) {
        this.listFromServer = listFromServer;
    }

    public void start(){
        Thread thread = new Thread(()->{
            List<Path> fileList = new ArrayList<>();
            creatListFile(Path.of(ClientProperties.getInstance().getRootDir()),fileList);
            fileList.forEach(s-> System.out.println(s.toString()));
            ArrayList<Path> pathsFromServer = new ArrayList<>();
            listFromServer.forEach(s->pathsFromServer.add(Path.of(s.getRelativizePath()).resolve(
                    ClientProperties.getInstance().getRootDir()
            )));
            System.out.println("после сравнения двух списков");
            fileList.removeAll(pathsFromServer);
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void creatListFile(Path dir,List<Path> fileList){
        try(DirectoryStream<Path> files = Files.newDirectoryStream(dir);) {
           files.forEach(new Consumer<Path>() {
               @Override
               public void accept(Path path) {
                   if(Files.isDirectory(path)){
                       creatListFile(path,fileList);
                   }else {
                       fileList.add(path);
                   }
               }
           });
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

}
