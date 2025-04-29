package org.com.techsalesmanagerclient.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Data
public class NettyClient implements AutoCloseable {

    private static volatile NettyClient instance;
    private static final Object lock = new Object();

    private final EventLoopGroup group;
    private Channel channel;
    private volatile CompletableFuture<JsonMessage> currentResponseFuture;

    private NettyClient() throws InterruptedException {
        this.group = new NioEventLoopGroup();
        connect("localhost", 8080);
    }

    public static NettyClient getInstance() throws InterruptedException {
        NettyClient result = instance;
        if (result == null) {
            synchronized (lock) {
                result = instance;
                if (result == null) {
                    instance = result = new NettyClient();
                }
            }
        }
        return result;
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
                                .addLast(new StringDecoder(StandardCharsets.UTF_8))
                                .addLast(new StringEncoder(StandardCharsets.UTF_8))
                                .addLast(new JsonDecoder())
                                .addLast(new JsonEncoder())
                                .addLast(new ClientHandler());
                    }
                });

        try {
            this.channel = bootstrap.connect(host, port).sync().channel();
            log.info("Connected to {}:{}", host, port);
        } catch (Exception e) {
            group.shutdownGracefully();
            throw new InterruptedException("Failed to connect: " + e.getMessage());
        }
    }

    private class ClientHandler extends SimpleChannelInboundHandler<JsonMessage> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, JsonMessage msg) {
            log.debug("Received response: {}", msg);
            if (currentResponseFuture != null) {
                currentResponseFuture.complete(msg);
                currentResponseFuture = null; // Сбрасываем future после получения ответа
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Channel error", cause);
            if (currentResponseFuture != null) {
                JsonMessage error = new JsonMessage();
                error.setCommand("error");
                error.addData("reason", cause.getMessage());
                currentResponseFuture.complete(error);
                currentResponseFuture = null;
            }
            ctx.close();
        }
    }

    public JsonMessage sendRequest(JsonMessage request) throws IOException, TimeoutException {
        if (request == null || request.getCommand() == null) {
            throw new IllegalArgumentException("Invalid request");
        }

        if (!channel.isActive()) {
            throw new IOException("Channel is not active");
        }

        // Создаем новый future для этого запроса
        CompletableFuture<JsonMessage> responseFuture = new CompletableFuture<>();
        this.currentResponseFuture = responseFuture;

        // Отправка сообщения
        channel.writeAndFlush(request).addListener(future -> {
            if (!future.isSuccess()) {
                String errorMsg = future.cause() != null ?
                        future.cause().getMessage() : "Unknown error";
                log.error("Send failed: {}", errorMsg);

                JsonMessage error = new JsonMessage();
                error.setCommand("error");
                error.addData("reason", errorMsg);
                responseFuture.complete(error);
            }
        });

        try {
            return responseFuture.get(8, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        } catch (ExecutionException e) {
            throw new IOException("Request failed", e);
        } catch (TimeoutException e) {
            currentResponseFuture = null; // Очищаем future при таймауте
            throw new TimeoutException("Response timeout");
        }
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close().syncUninterruptibly();
        }
        group.shutdownGracefully();
        log.info("Client shutdown complete");
    }
}