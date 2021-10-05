package appServer.handlersForClientSrv;

import appServer.handlersForMonitoringSrv.ServiceForMonitoring;
import appServer.serviceApp.EntityUser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import message.AuthRequest;
import message.AuthResponse;
import message.Command;
import message.CommandType;


@Slf4j
public class AuthenticationHandlerSrv extends SimpleChannelInboundHandler<Command> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        if (command.getCommandType() == CommandType.AUTH_REQUEST){
            AuthRequest authRequest = (AuthRequest) command;
            EntityUser user = new EntityUser(authRequest.getLogin(), authRequest.getHashPassword(),
                    authRequest.getSalt());
            AuthResponse authResponse = new AuthResponse();
            if (user.isAuthentication()){
                user.setIpAddress(ctx.channel().remoteAddress().toString());
                authResponse.setCode(200);
                log.info("Пользователь "+authRequest.getLogin()+" авторизовался");
                ctx.pipeline().remove(this);
                ctx.pipeline().addLast(new MessageHandler(user));
                //ctx.pipeline().addFirst(new SocketAccounting(new ServiceForMonitoring()));
                System.out.println(ctx.pipeline().names());
            }else {
                authResponse.setCode(404);
            }
            ctx.writeAndFlush(authResponse);
        }
    }

}
