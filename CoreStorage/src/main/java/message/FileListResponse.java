package message;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class FileListResponse extends Command{
    @Getter
    private final List<FileInfo> fileList = new ArrayList<>();

    public FileListResponse() {
        super.setCommandType(CommandType.LIST_FILE_RESPONSE);
    }
}
