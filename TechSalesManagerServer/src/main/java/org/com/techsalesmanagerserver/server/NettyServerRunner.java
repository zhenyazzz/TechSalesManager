package org.com.techsalesmanagerserver.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import org.com.techsalesmanagerserver.server.init.NettyServerInitializer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NettyServerRunner {
    private final ServerBootstrap serverBootstrap;
    private final NettyServerInitializer initializer;


    public NettyServerRunner(ServerBootstrap serverBootstrap, NettyServerInitializer initializer) {
        this.serverBootstrap = serverBootstrap;
        this.initializer = initializer;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() throws InterruptedException {
        serverBootstrap.childHandler(initializer); // Настраиваем обработчик
        ChannelFuture future = serverBootstrap.bind(8080).sync(); // Запускаем сервер на порту 8080
        future.channel().closeFuture().sync(); // Ждём, пока сервер не закроют
    }
}
