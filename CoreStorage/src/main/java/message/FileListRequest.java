package message;



public class FileListRequest extends Command{
    public FileListRequest() {
        super.setCommandType(CommandType.LIST_FILE_REQUEST);
    }

}
