package message;

import lombok.Getter;
import lombok.Setter;

public class AuthRequest extends Command {
    @Getter@Setter
    private String login;
    @Getter@Setter
    private byte[] hashPassword = new byte[16];
    @Getter@Setter
    private byte[] salt = new byte[16];

    public AuthRequest(){
        super.setCommandType(CommandType.AUTH_REQUEST);
    }

}
