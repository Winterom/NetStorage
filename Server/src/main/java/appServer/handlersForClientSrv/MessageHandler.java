package appServer.handlersForClientSrv;

import appServer.EntityUser;
import appServer.handlersForMonitoringSrv.ServiceForMonitoring;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.Command;

public class MessageHandler extends SimpleChannelInboundHandler<Command> {
   EntityUser user;
   ServiceForMonitoring service;
    public MessageHandler(EntityUser user, ServiceForMonitoring service){
        this.user = user;
        this.service = service;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Command command) throws Exception {

    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        service.getUsers().add(user);

    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        service.getUsers().remove(user);
    }
}
