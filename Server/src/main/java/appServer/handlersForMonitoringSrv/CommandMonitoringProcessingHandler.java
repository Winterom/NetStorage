package appServer.handlersForMonitoringSrv;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class CommandMonitoringProcessingHandler extends ChannelInboundHandlerAdapter {
    ServiceForMonitoring service;
    public CommandMonitoringProcessingHandler(ServiceForMonitoring service) {
        this.service = service;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String command = ((String) msg).trim();

        System.out.println(command);
        switch (command) {
            //Количество соединений на сервере
            case "con" -> ctx.writeAndFlush("Соединений на сервере: " + service.getCountConnection().get());
            case "auth" -> ctx.writeAndFlush("Авторизованных пользователей:" + service.getUsers().size());
            case "authList" -> ctx.writeAndFlush("Перечень авторизованных пользователей");
            default -> ctx.writeAndFlush("Неизвестная комманда");
        }
    }
}
