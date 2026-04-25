package org.delicias.delivery_users.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.delicias.exception.ErrorResponse;

@Provider
public class EmailAlreadyExistsExceptionMapper implements ExceptionMapper<EmailAlreadyExistsException> {

    @Override
    public Response toResponse(EmailAlreadyExistsException exception) {
        ErrorResponse error = new ErrorResponse(
                exception.getMessage(),
                exception.getErrorCode()
        );

        return Response.status(exception.getStatus())
                .entity(error)
                .build();
    }
}
