package message;


import java.time.LocalDateTime;

public class FileMessageHeader extends Command{
    @lombok.Setter @lombok.Getter
    private int quantityParts; // на какое количество пакетов будет разбит файл
    @lombok.Setter @lombok.Getter
    private long size; //храним в базе  данных в последствии определим объем всех файлов пользователя.
    @lombok.Setter @lombok.Getter
    private LocalDateTime lastModified; //храним в базе  данных
    @lombok.Setter @lombok.Getter
    private String relativizePath;//храним в базе  данных
    @lombok.Setter @lombok.Getter
    private long crc32file;

    public FileMessageHeader(){
        super.setCommandType(CommandType.FILE_MESSAGE_HEADER);
    }
}
