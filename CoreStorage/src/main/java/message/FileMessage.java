package message;


import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends Command{
    public FileInfo fileInfo;
    @Getter
    private final byte[] buffer;

    public FileMessage(FileInfo fileInfo) throws IOException {
        super.setCommandType(CommandType.FILE_MESSAGE);
        this.fileInfo = fileInfo;
        this.buffer = Files.readAllBytes(Path.of(fileInfo.getFullPath()));
    }
}
