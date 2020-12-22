package com.yuditsky.socketapp.service;

import com.yuditsky.socketapp.exception.ServiceException;

import java.io.IOException;

public interface ServerService {
    String read() throws ServiceException;

    void connect() throws ServiceException;

    void close() throws ServiceException;

    void init() throws ServiceException;

    void echo() throws ServiceException, IOException, InterruptedException;

    void time() throws ServiceException, IOException;

    void upload() throws ServiceException, IOException, InterruptedException;

    void download() throws ServiceException, IOException, InterruptedException;

}
