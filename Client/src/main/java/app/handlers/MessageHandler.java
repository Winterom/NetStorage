package app.handlers;


import app.NetCallback;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.Command;

public class MessageHandler extends SimpleChannelInboundHandler<Command> {
    private final NetCallback netCallback;

    public MessageHandler(NetCallback netCallback) {
        this.netCallback = netCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        netCallback.call(command);
    }
}
