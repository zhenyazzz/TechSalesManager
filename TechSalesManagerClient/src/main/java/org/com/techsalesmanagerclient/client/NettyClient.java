package org.com.techsalesmanagerclient.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.timeout.TimeoutException;
import lombok.Data;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class NettyClient implements AutoCloseable {
    private final EventLoopGroup group;
    private Channel channel;
    private volatile JsonMessage lastResponse;
    private final Object responseLock = new Object();

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
                                .addLast(new JsonObjectDecoder())
                                .addLast(new JsonDecoder())
                                .addLast(new JsonEncoder())
                                .addLast(new SimpleChannelInboundHandler<JsonMessage>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, JsonMessage msg) {
                                        synchronized (responseLock) {
                                            lastResponse = msg;
                                            responseLock.notifyAll();
                                        }
                                    }
                                });
                    }
                });

        this.channel = bootstrap.connect(host, port).sync().channel();
    }

    public JsonMessage sendRequest(JsonMessage request) throws IOException, TimeoutException {
        synchronized (responseLock) {
            lastResponse = null;
            channel.writeAndFlush(request).addListener(f -> {
                if (!f.isSuccess()) {
                    synchronized (responseLock) {
                        lastResponse = null;
                        responseLock.notifyAll();
                    }
                }
            });

            try {
                responseLock.wait(8000);
                return lastResponse;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Запрос прерван", e);
            }
        }
    }

    @Override
    public void close() {
        if (channel != null) channel.close();
        group.shutdownGracefully();
    }
}
