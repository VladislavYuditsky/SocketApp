package com.yuditsky.socketapp.service;

import com.yuditsky.socketapp.exception.ConnectionException;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.exception.ValidationException;

import java.io.DataInputStream;
import java.io.IOException;

public interface ClientService {

    boolean isConnected();

    void close() throws ServiceException, IOException;

    String echo(String message) throws ServiceException, IOException;

    String time() throws ServiceException, IOException;

    void upload(String filename) throws ServiceException, IOException, InterruptedException;

    boolean waitReconnecting(DataInputStream dataInputStream, byte[] ack) throws InterruptedException;

    void download(String filename) throws ServiceException, IOException, InterruptedException;

    void connect(String ip, int port) throws ValidationException, ConnectionException;
}
