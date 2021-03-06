package com.cnu.spg.user.exception;

import com.cnu.spg.comon.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    private static final String USER_NOT_FOUND_EXCEPTION_MSG = "user name not found exception";

    public UserNotFoundException() {
        super(USER_NOT_FOUND_EXCEPTION_MSG);
    }
}
