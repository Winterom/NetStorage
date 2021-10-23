package message;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Slf4j
public class FileInfo implements Serializable {


    //Основная цель по расширению подставлять соответствующие значки в таблицу
    public enum FileType {
        FILE, DIRECTORY, WORD,
        EXCEl, PDF, BMP, JPEG,
        GIF, PNG, PSD, MP3, AVI,
        ZIP, PPT, HTML, JS
    }

    @lombok.Setter
    @lombok.Getter
    private FileType fileType;
    @lombok.Setter
    @lombok.Getter
    private long size;
    @lombok.Setter
    @lombok.Getter
    private LocalDateTime lastModified;
    @lombok.Setter
    @lombok.Getter
    private int fileSynchronized;//1 -синхронизировано, 0 - не синхронизировано, 2 - это директория
    @lombok.Setter
    @lombok.Getter
    private String relativizePath;
    @lombok.Setter
    @lombok.Getter
    private String fullPath;


    public FileInfo(Path fullPath) {
        this.fullPath = fullPath.toString();
        try {
            this.size = Files.size(fullPath);
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(fullPath).toInstant(), ZoneId.systemDefault());
            if (Files.isDirectory(fullPath)) {
                this.fileType = FileType.DIRECTORY;
                this.size = -1;
                this.fileSynchronized = 2;
                return;
            }
            switch (getFileExtension(fullPath.getFileName().toString())) {
                case "doc", "docx" -> this.setFileType(FileType.WORD);
                case "xlsx", "xls", "xlsb", "xltx", "xlam" -> this.setFileType(FileType.EXCEl);
                case "pdf" -> this.setFileType(FileType.PDF);
                case "bmp" -> this.setFileType(FileType.BMP);
                case "jpeg" -> this.setFileType(FileType.JPEG);
                case "gif" -> this.setFileType(FileType.GIF);
                case "png" -> this.setFileType(FileType.PNG);
                case "psd" -> this.setFileType(FileType.PSD);
                case "mp3" -> this.setFileType(FileType.MP3);
                case "avi" -> this.setFileType(FileType.AVI);
                case "zip", "rar" -> this.setFileType(FileType.ZIP);
                case "ppt" -> this.setFileType(FileType.PPT);
                case "html" -> this.setFileType(FileType.HTML);
                case "js" -> this.setFileType(FileType.JS);
                default -> this.setFileType(FileType.FILE);
            }


        } catch (IOException e) {
            //в случае если файл перестал существовать
            //(обычно это ворд и эксель которые насоздают временных файлов а потом поудаляют нафиг)
            //поэтому watcherService может передать путь на уже не существующий файл
            //создаем элемент заглушку
            size =0;
            fileType = FileType.FILE;
            lastModified = LocalDateTime.now();
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public String getFileName() {
        return Path.of(fullPath).getFileName().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return lastModified.equals(fileInfo.lastModified) && relativizePath.equals(fileInfo.relativizePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastModified, relativizePath);
    }

    private static String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename);
    }
}
