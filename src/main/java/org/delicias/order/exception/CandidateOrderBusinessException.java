package org.delicias.order.exception;

import lombok.Getter;

public class CandidateOrderBusinessException extends RuntimeException {

    @Getter
    private final int status;

    private final CandidateOrderErrorCode errorCode;

    public CandidateOrderBusinessException(
            String message,
            CandidateOrderErrorCode errorCode,
            int status
    ) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() { return errorCode.name(); }
}
