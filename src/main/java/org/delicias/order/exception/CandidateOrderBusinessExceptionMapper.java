package org.delicias.order.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CandidateOrderBusinessExceptionMapper implements ExceptionMapper<CandidateOrderBusinessException> {

    @Override
    public Response toResponse(CandidateOrderBusinessException exception) {
        ErrorResponse error = new ErrorResponse(
                exception.getMessage(),
                exception.getErrorCode()
        );

        return Response.status(exception.getStatus())
                .entity(error)
                .build();
    }
}
