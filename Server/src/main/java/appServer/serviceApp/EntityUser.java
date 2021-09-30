package appServer.serviceApp;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

@Slf4j
public class EntityUser {
    public enum OS_TYPE{
        Android,
        Windows,
        OSXMACOS
    }
    @Getter@Setter
    private boolean isAuthentication;
    @Getter@Setter
    private String login; //храним в таблице users
    @Getter@Setter
    private byte[] salt;  //вычисляем или получаем в запросе
    @Getter@Setter
    private byte[] hashPassword;   //вычисляем или получаем в запросе
    @Getter@Setter
    private String password = "201219";//храним в таблице users
    @Getter@Setter
    private OS_TYPE platform;//храним в таблице connection
    @Getter@Setter
    private String ipAddress;//храним в таблице connection

    public EntityUser(String login, byte[] hashPassword, byte [] salt){
        this.salt = salt;
        this.login = login;
        this.hashPassword = hashPassword;
        isAuthentication = checkPassword();
    }

    private boolean checkPassword() {
        KeySpec spec = new PBEKeySpec(password.toCharArray(),
                salt,65536,128);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Arrays.equals(hashPassword,hash);
        }catch (NoSuchAlgorithmException | InvalidKeySpecException e){
            log.error(e.getMessage());
        }
        return false;
    }


}
