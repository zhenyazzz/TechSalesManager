package org.com.techsalesmanagerclient.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private final ObjectMapper mapper = new ObjectMapper();

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

    public List<Map<String, Object>> sendListRequest(JsonMessage request) throws IOException, TimeoutException {
        if (request == null || request.getCommand() == null) {
            throw new IllegalArgumentException("Invalid request");
        }
        if (!channel.isActive()) {
            throw new IOException("Channel is not active");
        }

        // Извлечение команды (например, "get_users" → "users")
        String command = request.getCommand();
        if (command.startsWith("get_")) {
            command = command.substring(4); // Убираем "get_"
        }
        String itemKey = command.substring(0, command.length() - 1); // Например, "users" → "user"

        List<Map<String, Object>> itemList = new ArrayList<>();
        CompletableFuture<JsonMessage> responseFuture = new CompletableFuture<>();
        this.currentResponseFuture = responseFuture;

        // Отправка запроса
        CompletableFuture<JsonMessage> finalResponseFuture = responseFuture;
        channel.writeAndFlush(request).addListener(future -> {
            if (!future.isSuccess()) {
                String errorMsg = future.cause() != null ? future.cause().getMessage() : "Unknown error";
                log.error("Send failed: {}", errorMsg);
                JsonMessage error = new JsonMessage();
                error.setCommand("error");
                error.addData("reason", errorMsg);
                finalResponseFuture.complete(error);
            }
        });

        try {
            while (true) {
                JsonMessage response = responseFuture.get(3, TimeUnit.SECONDS);
                this.currentResponseFuture = responseFuture = new CompletableFuture<>();

                if (("start_" + command).equals(response.getCommand())) {
                    log.info("Started receiving {}", command);
                } else if (itemKey.equals(response.getCommand())) {
                    Map<String, Object> item = mapper.convertValue(
                            response.getData().get("user"),
                            new TypeReference<Map<String, Object>>() {}
                    );
                    itemList.add(item);
                    //log.debug("Received {}: {}", itemKey, item);
                } else if (("end_" + command).equals(response.getCommand())) {
                    log.info("Finished receiving {}", command);
                    break;
                } else if ("error".equals(response.getCommand())) {
                    String reason = response.getData().get("reason").toString();
                    log.error("Server error: {}", reason);
                    throw new IOException("Server error: " + reason);
                } else {
                    log.error("Unexpected command: {}", response.getCommand());
                    throw new IOException("Unexpected command: " + response.getCommand());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        } catch (ExecutionException e) {
            throw new IOException("Request failed", e);
        } catch (TimeoutException e) {
            currentResponseFuture = null;
            throw new TimeoutException("Response timeout");
        }

        return itemList;
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