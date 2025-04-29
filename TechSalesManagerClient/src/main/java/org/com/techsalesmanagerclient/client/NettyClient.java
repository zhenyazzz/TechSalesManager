package org.com.techsalesmanagerclient.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;



import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

@Slf4j
@Data
public class NettyClient implements AutoCloseable {

    private final EventLoopGroup group;
    private Channel channel;

    public NettyClient() throws InterruptedException {
        this.group = new NioEventLoopGroup();
        connect("localhost", 8080);
    }

    public void connect(String host, int port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new LineBasedFrameDecoder(8192))
                                .addLast(new JsonDecoder())
                                .addLast(new StringDecoder(StandardCharsets.UTF_8)) // Преобразует ByteBuf в String
                                .addLast(new StringEncoder(StandardCharsets.UTF_8)) // Преобразует String в ByteBuf
                                .addLast(new JsonEncoder())
                                .addLast(new SimpleChannelInboundHandler<JsonMessage>() {
                                    private CompletableFuture<JsonMessage> responseFuture;

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) {
                                        responseFuture = new CompletableFuture<>();
                                        ctx.fireChannelActive();
                                    }

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, JsonMessage msg) {
                                        log.debug("Received response: {}", msg);
                                        if (responseFuture != null) {
                                            responseFuture.complete(msg);
                                            responseFuture = new CompletableFuture<>(); // Готовимся к следующему ответу
                                        }
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                        log.error("Channel error: {}", cause.getMessage(), cause);
                                        if (responseFuture != null) {
                                            JsonMessage error = new JsonMessage();
                                            error.setCommand("error");
                                            error.addData("reason", "Channel error: " + cause.getMessage());
                                            responseFuture.complete(error);
                                            responseFuture = new CompletableFuture<>();
                                        }
                                        ctx.close();
                                    }
                                });
                    }
                });

        try {
            this.channel = bootstrap.connect(host, port).sync().channel();
            log.info("Connected to {}:{}", host, port);
        } catch (Exception e) {
            group.shutdownGracefully();
            throw new InterruptedException("Failed to connect to " + host + ":" + port + ": " + e.getMessage());
        }
    }

    public JsonMessage sendRequest(JsonMessage request) throws IOException, TimeoutException {
        if (request == null || request.getCommand() == null) {
            throw new IllegalArgumentException("Request or command cannot be null");
        }

        if (!channel.isActive()) {
            throw new IOException("Channel is not active");
        }

        CompletableFuture<JsonMessage> responseFuture;
        try {
            SimpleChannelInboundHandler<JsonMessage> handler = (SimpleChannelInboundHandler<JsonMessage>) channel.pipeline().last();
            responseFuture = (CompletableFuture<JsonMessage>) handler.getClass().getDeclaredField("responseFuture").get(handler);
        } catch (Exception e) {
            log.error("Failed to access responseFuture: {}", e.getMessage(), e);
            throw new IOException("Internal error: failed to access response future", e);
        }


        //отправка сообщения
        channel.writeAndFlush(request).addListener(future -> {

            if (!future.isSuccess()) {
               // вызов ошибки
                String errorMessage = future.cause() != null ? future.cause().getMessage() : "Unknown error";
                if (errorMessage == null) {
                    errorMessage = "Failed to send request";
                }
                log.error("Failed to send request: {}", errorMessage);
                JsonMessage error = new JsonMessage();
                error.setCommand("error");
                error.addData("reason", errorMessage);
                responseFuture.complete(error);
            }
        });

        try {
            return responseFuture.get(8, TimeUnit.SECONDS); // Ожидаем ответа 8 секунд
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        } catch (TimeoutException e) {
            throw new TimeoutException("No response received within 8 seconds");
        } catch (Exception e) {
            throw new IOException("Failed to receive response: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (channel != null && channel.isOpen()) {
            channel.close().syncUninterruptibly();
            log.info("Channel closed");
        }
        group.shutdownGracefully();
        log.info("EventLoopGroup shut down");
    }
}