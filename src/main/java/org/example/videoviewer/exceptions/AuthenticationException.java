package org.example.videoviewer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.example.videoviewer.exceptions.Constants.Templates.AUTHENTICATION_EXCEPTION_TEMPLATE;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AuthenticationException extends BaseException {
    public AuthenticationException() {
        super(String.format(AUTHENTICATION_EXCEPTION_TEMPLATE, "Login or password is incorrect"));
    }

    public AuthenticationException(final String message) {
        super(String.format(AUTHENTICATION_EXCEPTION_TEMPLATE, message));
    }
}
