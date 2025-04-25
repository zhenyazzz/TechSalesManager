package org.com.techsalesmanagerserver.server.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.com.techsalesmanagerserver.server.handler.ServerHandler;
import org.com.techsalesmanagerserver.server.handler.UserHandler;
import org.com.techsalesmanagerserver.server.init.NettyServerInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyServerConfig {
    @Value("${netty.boss.threads:1}")  // Для принятия подключений
    private int bossThreads;

    @Value("${netty.worker.threads:4}") // Для обработки данных
    private int workerThreads;

    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossThreads);
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerThreads);
    }

    @Bean
    public UserHandler userHandler() {
        return new UserHandler();
    }
    @Bean
    public ServerHandler serverHandler() {
        return new ServerHandler();
    }
    @Bean
    public NettyServerInitializer nettyServerInitializer() {
        return new NettyServerInitializer(serverHandler());
    }

    // Конфигурация Netty
    @Bean
    public ServerBootstrap serverBootstrap() {
        return new ServerBootstrap()
                .group(bossGroup(), workerGroup())
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new NettyServerInitializer())
                .option(ChannelOption.SO_BACKLOG, 1000)
                .childOption(ChannelOption.TCP_NODELAY, true);
    }
}
