package com.yuditsky.socketapp.service;

import com.yuditsky.socketapp.exception.ConnectionException;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.exception.ValidationException;

public interface ClientService {

    void connect(String ip, int port) throws ValidationException, ConnectionException;

    boolean isConnected();

    void close() throws ServiceException;

    String echo(String message) throws ServiceException;

    String time() throws ServiceException;

    void upload(String filename) throws ServiceException;

    void download(String filename) throws ServiceException;
}
