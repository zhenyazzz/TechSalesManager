package org.com.techsalesmanagerserver.server;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.techsalesmanagerserver.enumeration.ResponseStatus;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response implements Serializable {
    private ResponseStatus status;
    private String body;
}
