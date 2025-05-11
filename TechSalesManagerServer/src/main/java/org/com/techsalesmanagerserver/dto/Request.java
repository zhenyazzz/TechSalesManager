package org.com.techsalesmanagerserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.com.techsalesmanagerserver.enumeration.RequestType;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Request implements Serializable{
    public Request() {
    }

    private RequestType type;
    private String body;
    private String token;
}
