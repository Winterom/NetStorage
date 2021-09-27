package message;

import lombok.Getter;
import lombok.Setter;

public class Authentication extends Command {
    @Getter@Setter
    private String login;
    @Getter@Setter
    private String hashPassword;
    @Getter@Setter
    private String responseCode;// 200 все отлично, 404 не прошли проверку.

    public Authentication(){
        super.setCommandType(CommandType.AUTH);
    }

}
