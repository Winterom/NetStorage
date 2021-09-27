package appServer.handlersForClientSrv;

import appServer.EntityUser;
import appServer.handlersForMonitoringSrv.ServiceForMonitoring;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.Authentication;
import message.Command;
import message.CommandType;

import java.nio.file.Path;

public class AuthenticationHandler extends SimpleChannelInboundHandler<Command> {
    private Path userDir;//потом создадим каталог для каждого юзверя
    private ServiceForMonitoring service;

    public AuthenticationHandler(Path pathRootDir, ServiceForMonitoring service){
        this.userDir = pathRootDir;
        this.service = service;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        if (command.getType() == CommandType.AUTH){
            Authentication authentication = (Authentication) command;
            EntityUser user = new EntityUser(authentication.getLogin(), authentication.getHashPassword());
            if (user.isAuthentication()){
                user.setIpAddress(ctx.channel().remoteAddress().toString());
                System.out.println(user.getIpAddress());
                ctx.pipeline().remove(this);
                ctx.pipeline().addLast(new MessageHandler(user,service));

            }
        }
    }

}
