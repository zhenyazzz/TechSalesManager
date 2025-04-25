package org.com.techsalesmanagerserver.server.init;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class JsonEncoder extends MessageToMessageEncoder<JsonMessage> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx, JsonMessage msg, List<Object> out) {
        try {
            String json = mapper.writeValueAsString(msg);
            out.add(json + "\n"); // Добавляем символ новой строки
        } catch (JsonProcessingException e) {
            ctx.fireExceptionCaught(e);
        }
    }
}
