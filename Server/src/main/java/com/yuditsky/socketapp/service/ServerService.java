package com.yuditsky.socketapp.service;

import com.yuditsky.socketapp.exception.ServiceException;

public interface ServerService {
    String read() throws ServiceException;

    void connect() throws ServiceException;

    void close() throws ServiceException;

    void init() throws ServiceException; //Мб не нужен будет в UDP

    void echo() throws ServiceException;

    void time() throws ServiceException;

    void upload() throws ServiceException;

    void download() throws ServiceException;

}
