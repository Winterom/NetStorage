package appServer;

import at.favre.lib.crypto.bcrypt.BCrypt;
import lombok.Getter;
import lombok.Setter;

public class EntityUser {
    public enum OS_TYPE{
        Android,
        Windows,
        OSXMACOS
    }
    @Getter@Setter
    private boolean isAuthentication;
    @Getter@Setter
    private String login;
    @Getter@Setter
    private String hashPassword;
    @Getter@Setter
    private OS_TYPE platform;
    @Getter@Setter
    private String ipAddress;

    public EntityUser(String login, String hashPassword){

        this.login = login;
        this.hashPassword = hashPassword;
        isAuthentication = checkPassword();
    }

    private boolean checkPassword() {
        return true;
    }


}
