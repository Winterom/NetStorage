package message;

import lombok.Getter;
import lombok.Setter;

public class FileMessagePart extends Command{
    @Getter@Setter
    private byte[] buffer;
    @Getter@Setter
    private int numberOfPart;//номер текущего пакета

    public FileMessagePart() {
        super.setCommandType(CommandType.FILE_MESSAGE_PART);
    }
}
