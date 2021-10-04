package message;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
public class FileInfo implements Serializable {


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
    private FileType fileType;
    @lombok.Setter @lombok.Getter
    private long size;
    @lombok.Setter @lombok.Getter
    private LocalDateTime lastModified;
    @lombok.Setter @lombok.Getter
    private boolean fileSynchronized;
    @lombok.Setter @lombok.Getter
    private String relativizePath;
    @lombok.Setter @lombok.Getter
    private String fullPath;


    public FileInfo(Path fullPath) {
        this.fullPath = fullPath.toString();
        try {
            this.size = Files.size(fullPath);
            this.fileType = Files.isDirectory(fullPath)?FileType.DIRECTORY:FileType.FILE;
            if (fileType==FileType.DIRECTORY)
                this.size = -1;
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(fullPath).toInstant(), ZoneId.systemDefault());

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    public String getFileName() {
        return Path.of(fullPath).getFileName().toString();
    }
}
