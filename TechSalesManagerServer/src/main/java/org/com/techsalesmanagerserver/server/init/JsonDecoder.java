package org.com.techsalesmanagerserver.server.init;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class JsonDecoder extends MessageToMessageDecoder<String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) {
        try {
            JsonMessage message = mapper.readValue(msg, JsonMessage.class);
            out.add(message);
        } catch (JsonProcessingException e) {
            ctx.fireExceptionCaught(new IllegalArgumentException("Invalid JSON format"));
        }
    }
}
