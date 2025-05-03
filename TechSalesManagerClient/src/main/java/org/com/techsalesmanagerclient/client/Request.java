package org.com.techsalesmanagerclient.client;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.com.techsalesmanagerclient.enums.RequestType;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Request implements Serializable{
    private RequestType type;
    private String body;
}
