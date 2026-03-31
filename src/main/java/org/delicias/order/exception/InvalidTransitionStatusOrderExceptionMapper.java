package org.delicias.order.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidTransitionStatusOrderExceptionMapper implements ExceptionMapper<InvalidTransitionStatusOrderException> {
    @Override
    public Response toResponse(InvalidTransitionStatusOrderException exception) {
        ErrorResponse error = new ErrorResponse(
                exception.getMessage(),
                exception.getErrorCode()
        );

        return Response.status(exception.getStatus())
                .entity(error)
                .build();
    }
}
