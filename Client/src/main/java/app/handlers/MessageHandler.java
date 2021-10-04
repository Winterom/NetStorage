package app.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import lombok.extern.slf4j.Slf4j;
import message.Command;


@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Command> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        System.out.println(msg);
    }
}
