package app.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import message.AuthResponse;
import message.Command;



@Slf4j
public class AuthenticationHandler extends SimpleChannelInboundHandler<Command> {


    public AuthenticationHandler( ) {

    }
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        AuthResponse request = (AuthResponse) command;
        if (request.getCode()==200){
            log.info("Успешная аутентификация");
            ctx.channel().pipeline().remove(this).addLast(new MessageHandler());
            System.out.println(ctx.pipeline().names());
        }else
        {
            //вывести сообщение об ошибки аутентификации и об отсутствии синхронизации
            log.info("Пароль или логин не верен");
        }
    }
}
