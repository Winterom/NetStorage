package message;


import lombok.Getter;
import lombok.Setter;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

public class FileMessage extends Command{
    @lombok.Setter @lombok.Getter
    private long size; //храним в базе  данных
    @lombok.Setter @lombok.Getter
    private LocalDateTime lastModified; //храним в базе  данных
    @lombok.Setter @lombok.Getter
    private String relativizePath;//храним в базе  данных
    @Getter@Setter
    private boolean isFinal = false;
    @Getter
    private final ByteBuffer buffer;

    public FileMessage(int bufferSize){
        super.setCommandType(CommandType.FILE_MESSAGE);
        buffer  = ByteBuffer.allocate(bufferSize);
    }
}
