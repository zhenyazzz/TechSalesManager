package org.com.techsalesmanagerserver.server.init;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;
import org.com.techsalesmanagerserver.controller.AuthController;
import org.com.techsalesmanagerserver.controller.CommandRouter;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


@RequiredArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        List<CommandController> controllers = Arrays.asList(
                new AuthController()
                // + другие контроллеры...
        );
        ch.pipeline()
                .addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                .addLast(new StringDecoder(StandardCharsets.UTF_8))
                .addLast(new StringEncoder(StandardCharsets.UTF_8))
                .addLast(new JsonDecoder())
                .addLast(new JsonEncoder())
                .addLast(new CommandRouter(controllers));
        
    }
}
