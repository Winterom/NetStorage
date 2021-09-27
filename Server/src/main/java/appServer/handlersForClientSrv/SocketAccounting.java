package appServer.handlersForClientSrv;

import appServer.handlersForMonitoringSrv.ServiceForMonitoring;
import io.netty.channel.*;

public class SocketAccounting extends ChannelInboundHandlerAdapter {
    ServiceForMonitoring service;

    public SocketAccounting(ServiceForMonitoring service) {
        this.service = service;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       super.channelActive(ctx);
       service.getCountConnection().incrementAndGet();
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        service.getCountConnection().decrementAndGet();
    }
}
