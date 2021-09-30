package app.handlers;

import app.ClientProperties;
import app.NetCallback;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import message.AuthRequest;
import message.AuthResponse;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

@Slf4j
public class AuthenticationHandler extends ChannelInboundHandlerAdapter {

    private final NetCallback netCallback;
    boolean isAuth;

    public AuthenticationHandler(boolean isAuth, NetCallback netCallback) {
        this.isAuth =isAuth;
        this.netCallback = netCallback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //super.channelActive(ctx);
        AuthRequest authRequest = new AuthRequest();
        authRequest.setLogin(ClientProperties.getInstance().getLogin());
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(ClientProperties.getInstance().getPassword().toCharArray(),
                salt,65536,128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        authRequest.setSalt(salt);
        authRequest.setHashPassword(hash);
        ctx.writeAndFlush(authRequest);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        AuthResponse request = (AuthResponse) msg;
        if (request.getCode()==200){
            isAuth = true;
            log.info("Успешная аутентификация");
            ctx.channel().pipeline().remove(this).addLast(new MessageHandler(netCallback));
        }else
        {
            //вывести сообщение об ошибки аутентификации и об отсутствии синхронизации
            log.info("Пароль или логин не верен");
        }
    }
}
