package org.com.techsalesmanagerclient.client;


import lombok.Data;
import org.com.techsalesmanagerclient.enums.ResponseStatus;

import java.io.Serializable;

@Data
public class Response implements Serializable {
    private ResponseStatus status;
    private String body;
}
