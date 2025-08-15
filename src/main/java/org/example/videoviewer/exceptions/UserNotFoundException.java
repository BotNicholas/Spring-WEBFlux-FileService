package org.example.videoviewer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.example.videoviewer.exceptions.Constants.Templates.AUTHENTICATION_EXCEPTION_TEMPLATE;
import static org.example.videoviewer.exceptions.Constants.Templates.USER_NOT_FOUND_ERROR_TEMPLATE;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UserNotFoundException extends BaseException {
    public UserNotFoundException(final String username) {
        super(String.format(USER_NOT_FOUND_ERROR_TEMPLATE, String.format("'%s'", username)));
    }

    public UserNotFoundException() {
        super(USER_NOT_FOUND_ERROR_TEMPLATE);
    }
}
