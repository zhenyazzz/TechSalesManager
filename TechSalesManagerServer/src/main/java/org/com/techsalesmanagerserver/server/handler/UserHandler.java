package org.com.techsalesmanagerserver.server.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.com.techsalesmanagerserver.server.init.JsonMessage;
import org.com.techsalesmanagerserver.service.UserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHandler implements MessageHandler {
    private final UserService userService;



    @Override
    public void handle(ChannelHandlerContext ctx, JsonMessage msg) {
        if ("login".equals(msg.getCommand())) {
            JsonMessage response = new JsonMessage();
            response.setCommand("login");
            response.setData(msg.getData());
            ctx.writeAndFlush(response);
        }
    }
}
