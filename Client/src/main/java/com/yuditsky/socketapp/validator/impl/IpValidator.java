package com.yuditsky.socketapp.validator.impl;

import com.yuditsky.socketapp.exception.ValidationException;
import com.yuditsky.socketapp.validator.Validator;

public class IpValidator implements Validator<String> {
    @Override
    public void validate(String ip) throws ValidationException {
        if(!isValid(ip)){
            throw new ValidationException("Invalid IP address {0}", ip);
        }
    }

    private boolean isValid(String ip){
        return true;
    }
}
