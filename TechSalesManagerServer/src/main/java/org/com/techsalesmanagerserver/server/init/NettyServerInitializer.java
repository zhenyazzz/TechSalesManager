package org.com.techsalesmanagerserver.server.init;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;
import org.com.techsalesmanagerserver.controller.CommandRouter;
import org.com.techsalesmanagerserver.controller.Controller;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final CommandRouter commandRouter;


    @Override
    protected void initChannel(SocketChannel ch) {

        ch.pipeline()
               // .addLast(new JsonObjectDecoder())
                .addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                .addLast(new StringDecoder(StandardCharsets.UTF_8))
                .addLast(new StringEncoder(StandardCharsets.UTF_8))
                .addLast(new JsonDecoder())
                .addLast(new JsonEncoder())
                .addLast(commandRouter);


    }
}
