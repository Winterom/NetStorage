package appServer.handlersForMonitoringSrv;

import appServer.EntityUser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Iterator;

public class CommandMonitoringProcessingHandler extends ChannelInboundHandlerAdapter {
    ServiceForMonitoring service;
    public CommandMonitoringProcessingHandler(ServiceForMonitoring service) {
        this.service = service;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String command = ((String) msg).trim();

        System.out.println(command);
        switch (command){
            //Количество соединений на сервере
            case "con":
                ctx.writeAndFlush("Соединений на сервере: "+service.getCountConnection().get()+'\n');
                break;
            case "auth":
                ctx.writeAndFlush("Авторизованных пользователей:"+service.getUsers().size()+'\n');
                break;
            case "authList":
                ctx.writeAndFlush("Перечень авторизованных пользователей"+'\n');
                Iterator<EntityUser> iterator = service.getUsers().iterator();
                while (iterator.hasNext()){
                    EntityUser user = iterator.next();
                    ctx.writeAndFlush("Пользователь: "+user.getLogin()+ "ip адрес: "+user.getIpAddress()+'\n');
                }
                break;
            default:
                ctx.writeAndFlush("Неизвестная комманда"+'\n');
        }
    }
}
