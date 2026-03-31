package org.delicias.order.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class InvalidTransitionStatusOrderException extends RuntimeException {
    private final int status;

    private final String errorCode;

    public InvalidTransitionStatusOrderException(
            String orderStatus
    ) {
        super("Can´t transition to "+ orderStatus);
        this.errorCode = "ORD_STATUS";
        this.status = Response.Status.BAD_REQUEST.getStatusCode();
    }

}
