package appServer.serviceApp;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

@Slf4j
public class EntityUser {
    @Getter@Setter
    private boolean isAuthentication;
    @Getter@Setter
    private int id;
    @Getter@Setter
    private String login; //храним в таблице users
    @Getter@Setter
    private byte[] salt;  //вычисляем или получаем в запросе
    @Getter@Setter
    private byte[] hashPassword;   //вычисляем или получаем в запросе
    @Getter@Setter
    private String password ;//храним в таблице users

    public EntityUser(String login, byte[] hashPassword, byte [] salt){
        this.salt = salt;
        this.login = login;
        this.hashPassword = hashPassword;
        isAuthentication = checkPassword();
    }

    private boolean checkPassword() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT password, id from users where login=? and isactive=true;");
            stm.setString(1,login);
            ResultSet rs = stm.executeQuery();
            if(rs.next()){
                this.password = rs.getString("password");
                this.id = rs.getInt("id");
                connection.close();
            }else{
                connection.close();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
