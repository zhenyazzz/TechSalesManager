package org.com.techsalesmanagerserver.server.init;

import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class JsonMessage {
    private String command;
    private Map<String, Object> data = new HashMap<String, Object>();

}
