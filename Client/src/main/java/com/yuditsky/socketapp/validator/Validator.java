package com.yuditsky.socketapp.validator;

import com.yuditsky.socketapp.exception.ValidationException;

public interface Validator<T> {
    void validate(T object) throws ValidationException;
}
