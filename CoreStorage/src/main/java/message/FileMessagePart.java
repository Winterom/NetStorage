package message;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class FileMessagePart extends Command implements Serializable {
    @Getter@Setter
    private byte[] buffer;//буфер с данными
    @Getter@Setter
    private int numberOfPart;//номер текущего пакета
    @Getter@Setter
    private int countOfData;//колличество данных в буфере

    public FileMessagePart() {
        super.setCommandType(CommandType.FILE_MESSAGE_PART);
    }
}
