package org.com.techsalesmanagerclient.client;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class JsonMessage {
    private String command;
    private Map<String, Object> data = new HashMap<String, Object>();
    public void addData(String key, Object value) {
        data.put(key, value);
    }
}
