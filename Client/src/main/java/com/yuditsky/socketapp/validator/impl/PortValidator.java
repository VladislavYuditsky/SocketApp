package com.yuditsky.socketapp.validator.impl;

import com.yuditsky.socketapp.exception.ValidationException;
import com.yuditsky.socketapp.validator.Validator;

public class PortValidator implements Validator<Integer> {
    @Override
    public void validate(Integer port) throws ValidationException {
        if(!isValid(port)){
            throw new ValidationException("Invalid port {0}", port);
        }
    }

    private boolean isValid(Integer port) {
        return (port >= 0 & port < 65536);
    }
}
