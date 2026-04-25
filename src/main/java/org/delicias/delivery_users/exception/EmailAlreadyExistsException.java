package org.delicias.delivery_users.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException{

    private final int status;
    private final String errorCode;


    public EmailAlreadyExistsException(int status, String username) {
        super(String.format("email: %s already exists", username));
        this.status = status;
        this.errorCode = "EmailAlreadyExistsException";
    }
}
