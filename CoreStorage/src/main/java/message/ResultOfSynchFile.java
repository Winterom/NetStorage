package message;

import lombok.Getter;
import lombok.Setter;

public class ResultOfSynchFile extends Command{
    @Setter @Getter
    private String relativizePath;//файл о котором идет речь в сообщении
    @Getter@Setter
    private int codeOfResult;
    //200 - все ок, только в этом случае файл синхронизирован
    //400 - не совпадает размер исходного файла и файла на сервере
    //405 - не возможно создать файл на сервере
    //440 - иная ошибка на сервере

    public ResultOfSynchFile(int codeOfResult) {
        super.setCommandType(CommandType.RESULT_OF_SYNCH_FILE);
        this.codeOfResult = codeOfResult;
    }
}
