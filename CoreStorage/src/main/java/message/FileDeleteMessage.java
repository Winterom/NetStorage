package message;

import lombok.Getter;

import java.util.ArrayList;

public class FileDeleteMessage extends Command{
    @Getter
    private ArrayList<String> pathList = new ArrayList<>();

    public FileDeleteMessage() {
        super.setCommandType(CommandType.FILE_DELETE_MESSAGE);
    }
}
