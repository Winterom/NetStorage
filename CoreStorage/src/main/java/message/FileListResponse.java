package message;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class FileListResponse extends Command{
    @Getter
    private Map<String, LocalDateTime> fileList = new HashMap<>();

    public FileListResponse() {
        super.setCommandType(CommandType.LIST_FILE_RESPONSE);
    }
}
