package message;

import lombok.Getter;
import lombok.Setter;

public class AuthResponse extends Command{
    @Getter@Setter
    int code;
    public AuthResponse() {
        super.setCommandType(CommandType.AUTH_RESPONSE);
    }
}
