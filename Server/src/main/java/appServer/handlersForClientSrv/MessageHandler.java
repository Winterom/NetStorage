package appServer.handlersForClientSrv;

import appServer.serviceApp.EntityUser;
import appServer.handlersForMonitoringSrv.ServiceForMonitoring;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.Command;

import java.time.LocalDateTime;

public class MessageHandler extends SimpleChannelInboundHandler<Command> {
   EntityUser user;
   ServiceForMonitoring service;
    public MessageHandler(EntityUser user, ServiceForMonitoring service){
        this.user = user;
        this.service = service;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Command command) throws Exception {
        switch (command.getCommandType()) {
            case LIST_FILE_REQUEST:
        }

    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //super.channelActive(ctx);
        service.getUsers().put(user, LocalDateTime.now());

    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //super.channelInactive(ctx);
        service.getUsers().remove(user);
    }
}
