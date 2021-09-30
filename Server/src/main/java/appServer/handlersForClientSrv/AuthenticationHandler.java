package appServer.handlersForClientSrv;

import appServer.serviceApp.EntityUser;
import appServer.handlersForMonitoringSrv.ServiceForMonitoring;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.AuthRequest;
import message.AuthResponse;
import message.Command;
import message.CommandType;


import java.nio.file.Path;

public class AuthenticationHandler extends SimpleChannelInboundHandler<Command> {

    private final ServiceForMonitoring service;

    public AuthenticationHandler( ServiceForMonitoring service){

        this.service = service;
    }
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
                ctx.pipeline().remove(this);
                ctx.pipeline().addLast(new MessageHandler(user,service));
            }else {
                authResponse.setCode(404);
            }
            ctx.writeAndFlush(authResponse);
        }
    }

}
