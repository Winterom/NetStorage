package app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class FileInfo {

    public enum FileType { FILE("F"), DIRECTORY ("D"),WORD("W"),
        EXCEl("E"),PDF("PDF"),BMP("BMP"),JPEG("JPEG"),
        GIF("GIF"),PNG("PNG"),PSD("PSD"),MP3("MP3"),AVI("AVI"),
        ZIP("ZIP"),PPT("PPT"),HTML("HTML"),JS("JS"), ANY("ANY");
        private String name;
        FileType (String name) {
            this.name = name;
        }

        public String getName() {
                return name;
        }
    }

    private String fileName;
    private FileType fileType;
    private long size;
    private LocalDateTime lastModified;
    private boolean fileSynchronized;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isFileSynchronized() {
        return fileSynchronized;
    }

    public void setFileSynchronized(boolean fileSynchronized) {
        this.fileSynchronized = fileSynchronized;
    }

    public FileInfo(Path path) {
        this.fileName = path.getFileName().toString();
        this.size = Files.size(path);
    }
}
