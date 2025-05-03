package org.com.techsalesmanagerserver.server;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.com.techsalesmanagerserver.enumeration.RequestType;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Request implements Serializable{
    private RequestType type;
    private String body;
}
