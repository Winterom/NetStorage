package app;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
public class FileInfo {
    //Основная цель по расширению подставлять соответствующие значки в таблицу
    public enum FileType { FILE("F"), DIRECTORY ("D"),WORD("W"),
        EXCEl("E"),PDF("PDF"),BMP("BMP"),JPEG("JPEG"),
        GIF("GIF"),PNG("PNG"),PSD("PSD"),MP3("MP3"),AVI("AVI"),
        ZIP("ZIP"),PPT("PPT"),HTML("HTML"),JS("JS");
        private final String name;
        FileType (String name) {
            this.name = name;
        }

        public String getName() {
                return name;
        }
    }

    @lombok.Setter @lombok.Getter
    private String fileName;
    @lombok.Setter @lombok.Getter
    private FileType fileType;
    @lombok.Setter @lombok.Getter
    private long size;
    @lombok.Setter @lombok.Getter
    private LocalDateTime lastModified;
    @lombok.Setter @lombok.Getter
    private boolean fileSynchronized;


    public FileInfo(Path path) {
        this.fileName = path.getFileName().toString();
        try {
            this.size = Files.size(path);
            this.fileType = Files.isDirectory(path)?FileType.DIRECTORY:FileType.FILE;
            if (fileType==FileType.DIRECTORY)
                this.size = -1;
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault());

        } catch (IOException e) {
            log.error("Stacktrace ",e);
        }
    }
}
