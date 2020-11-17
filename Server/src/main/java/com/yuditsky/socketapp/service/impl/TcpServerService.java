package com.yuditsky.socketapp.service.impl;

import com.yuditsky.socketapp.entity.TcpServer;
import com.yuditsky.socketapp.exception.ConnectionException;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.service.ServerService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Log4j2
@AllArgsConstructor
public class TcpServerService implements ServerService {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String STORE_PATH = "store/";

    private TcpServer server;

    @Override
    public String read() throws ServiceException {
        try {
            return server.getDataInputStream().readUTF();
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public void connect() throws ConnectionException {
        try {
            log.debug("Waiting");

            ServerSocket serverSocket = server.getServerSocket();

            Socket socket = serverSocket.accept();
            socket.setKeepAlive(true);
            socket.setSoTimeout(1000);

            server.setServerSocket(serverSocket);
            server.setSocket(socket);
            server.setDataInputStream(new DataInputStream(new BufferedInputStream(socket.getInputStream())));
            server.setDataOutputStream(new DataOutputStream(new BufferedOutputStream(socket.getOutputStream())));

            log.debug("Connected");
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ConnectionException("Connection failed", e);
        }
    }

    @Override
    public void close() throws ServiceException {
        try {
            server.getDataOutputStream().close();
            server.getDataInputStream().close();
            server.getSocket().close();
            log.debug("Connection closed");

            connect();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ServiceException(e);
        }
    }

    @Override
    public void init() throws ServiceException {
        try {
            ServerSocket serverSocket = new ServerSocket(server.getPort());
            server.setServerSocket(serverSocket);
            log.debug("Init server socket");
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ServiceException(e);
        }
    }

    @Override
    public void echo() throws ServiceException {
        try {
            String message = server.getDataInputStream().readUTF();
            log.debug("Echo message: " + message);

            DataOutputStream dataOutputStream = server.getDataOutputStream();
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        } catch (IOException e) {
            throw new ServiceException("Echo failed", e);
        }
    }

    @Override
    public void time() throws ServiceException {
        try {
            String time = LocalTime.now().format(dateTimeFormatter);
            log.debug("Time: " + time);

            DataOutputStream dataOutputStream = server.getDataOutputStream();
            dataOutputStream.writeUTF(time);
            dataOutputStream.flush();
        } catch (IOException e) {
            throw new ServiceException("Time failed", e);
        }
    }

    @Override
    public void upload() throws ServiceException {


//        ConnectionHandler connectionHandler = new ConnectionHandler();
        try {
            String filename = server.getDataInputStream().readUTF();
            long length = server.getDataInputStream().readLong();

            FileOutputStream fileOutputStream = new FileOutputStream(
//                    "C://Users//JFresh//Desktop//server_upload//" + filename
                    STORE_PATH + filename
            );

            int n;
            byte[] buf = new byte[4092];
            while (length != 0) {
//                try {
                if ((n = server.getDataInputStream().read(buf)) == -1) {
                    break;
                }
                fileOutputStream.write(buf, 0, n);
                fileOutputStream.flush();
                length -= n;
                System.out.println(length);
//                } catch (Exception e) {
//                    if (connectionHandler.closeConnection()) {
//                        fileOutputStream.close();
//                        close();
//                        connect();
//                        return;
//                    }
//                    continue;
//                }
            }
            fileOutputStream.close();

            server.getDataOutputStream().writeUTF("done");
            server.getDataOutputStream().flush();
        } catch (IOException e) {
            throw new ServiceException("Data stream IO exception. Upload failed", e);
        }
    }

    @Override
    public void download() throws ServiceException {
        String filename = null;
        try {
            filename = server.getDataInputStream().readUTF();
            DataOutputStream dataOutputStream = server.getDataOutputStream();
            File file;
//            try {
            //file = new File("server/files/" + filename);
//                file = new File("C://Users//JFresh//Desktop//server_upload//" + filename);
            file = new File(STORE_PATH + filename);
            dataOutputStream.writeBoolean(true);
            dataOutputStream.flush();
//            } catch (Exception e) {
//                dataOutputStream.writeBoolean(false);
//                dataOutputStream.flush();
//                return;
//            }
            System.out.println(file.length());
            dataOutputStream.writeLong(file.length());
            dataOutputStream.flush();

            FileInputStream fileInputStream = new FileInputStream(file);
            int n;
            byte[] buf = new byte[4092];
            while ((n = fileInputStream.read(buf)) != -1) {
                dataOutputStream.write(buf, 0, n);
                System.out.println(n);
                dataOutputStream.flush();
            }
        } catch (IOException e) {
            throw new ServiceException("Data stream IO exception. Download failed.", e);
        }
    }
}
