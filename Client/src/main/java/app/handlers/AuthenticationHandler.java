package app.handlers;

import clientGUI.MainController;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import message.AuthResponse;
import message.Command;



@Slf4j
public class AuthenticationHandler extends SimpleChannelInboundHandler<Command> {
    MainController mainController;

    public AuthenticationHandler(MainController mainController) {
        this.mainController = mainController;
    }
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        AuthResponse request = (AuthResponse) command;
        if (request.getCode()==200){
            log.info("Успешная аутентификация");
            mainController.messageLabel.setText("Успешная аутентификация");
            ctx.channel().pipeline().remove(this).addLast(new MessageHandler(mainController));
        }else
        {
            //вывести сообщение об ошибки аутентификации и об отсутствии синхронизации
            log.info("Пароль или логин не верен");
            mainController.messageLabel.setText("Пароль или логин не верен");
            mainController.synchButton.setDisable(true);
        }
    }
}
