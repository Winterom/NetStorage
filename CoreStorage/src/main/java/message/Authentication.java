package message;

import lombok.Getter;
import lombok.Setter;

public class Authentication extends Command {
    @Getter@Setter
    private String login;
    @Getter@Setter
    private String hashPassword;
    public Authentication(){
        super.setType(CommandType.AUTH);
    }

}
