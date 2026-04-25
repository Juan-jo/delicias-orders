package org.delicias.delivery_users.exception;

import lombok.Getter;

@Getter
public class UserNameAlreadyExistsException extends RuntimeException{

    private final int status;
    private final String errorCode;


    public UserNameAlreadyExistsException(int status, String username) {
        super(String.format("username: %s already exists", username));
        this.status = status;
        this.errorCode = "UserNameAlreadyExistsException";
    }
}
