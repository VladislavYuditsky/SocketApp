package com.yuditsky.socketapp.service.impl;

import com.yuditsky.socketapp.entity.TcpClient;
import com.yuditsky.socketapp.exception.ConnectionException;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.exception.ValidationException;
import com.yuditsky.socketapp.service.ClientService;
import com.yuditsky.socketapp.validator.impl.IpValidator;
import com.yuditsky.socketapp.validator.impl.PortValidator;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.Socket;
import java.util.Date;

@Log4j2
public class TcpClientService implements ClientService {

    private static final String BASKET_PATH = "basket/";

    private TcpClient client = new TcpClient();
    private IpValidator ipValidator = new IpValidator();
    private PortValidator portValidator = new PortValidator();

    @Override
    public void connect(String ip, int port) throws ValidationException, ConnectionException {
        ipValidator.validate(ip);
        portValidator.validate(port);

        try {
            Socket socket = new Socket(ip, port);
            socket.setKeepAlive(true);

            client.setSocket(socket);
            client.setDataInputStream(new DataInputStream(new BufferedInputStream(socket.getInputStream())));
            client.setDataOutputStream(new DataOutputStream(new BufferedOutputStream(socket.getOutputStream())));
            client.setConnected(true);
        } catch (IOException e) {
            throw new ConnectionException("Connection failed", e);
        }
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public void close() throws ServiceException {
        try {
            DataOutputStream dataOutputStream = client.getDataOutputStream();

            dataOutputStream.writeUTF("close");

            dataOutputStream.close();
            client.getDataOutputStream().close();
            client.getSocket().close();

            client.setConnected(false);
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public String echo(String message) throws ServiceException {
        String receivedMessage = "";
        log.debug("Sent message: " + message);

        try {
            DataOutputStream dataOutputStream = client.getDataOutputStream();

            dataOutputStream.writeUTF("echo");
            dataOutputStream.flush();
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();

            receivedMessage = client.getDataInputStream().readUTF();
            log.debug("Received message: " + receivedMessage);
        } catch (IOException e) {
            throw new ServiceException("Echo failed.", e);
        }

        return receivedMessage;
    }

    @Override
    public String time() throws ServiceException {
        String time = "";

        try {
            DataOutputStream dataOutputStream = client.getDataOutputStream();

            dataOutputStream.writeUTF("time");
            dataOutputStream.flush();

            time = client.getDataInputStream().readUTF();
            log.debug("Server time: " + time);
        } catch (IOException e) {
            throw new ServiceException("Time failed.", e);
        }

        return time;
    }

    @Override
    public void upload(String filename) throws ServiceException {
//        ConnectionHandler connectionHandler = new ConnectionHandler();
//        File file = new File("client/files/" + filename);
        File file = new File(BASKET_PATH + filename);

        Long startTime = new Date().getTime();

        DataOutputStream dataOutputStream = client.getDataOutputStream();
        try {

            //если передать имя несуществующего файла -> не IO ex и т.д., а файла с таким именем не существует
            // в exception + на сервере создаётся такой файл и остаётся мусор!

            dataOutputStream.writeUTF("upload");
            dataOutputStream.flush();
            dataOutputStream.writeUTF(file.getName());
            dataOutputStream.flush();
            dataOutputStream.writeLong(file.length());
            dataOutputStream.flush();

            FileInputStream fileInputStream = new FileInputStream(file);
            int n;
            byte[] buf = new byte[4092];
            while ((n = fileInputStream.read(buf)) != -1) {
//                try {
                Thread.sleep(5);
                dataOutputStream.write(buf, 0, n);
                System.out.println(n);
                dataOutputStream.flush();
//                } catch (Exception e) {
//                    if (connectionHandler.closeConnection()) {
//                        return;
//                    }
//                    Long position = fileInputStream.getChannel().position();
//                    fileInputStream.getChannel().position(position - n);
//                }
            }

            if (client.getDataInputStream().readUTF().equals("done")) {
                Long endTime = new Date().getTime();
                System.out.println("BITRATE: " + ((double) file.length() / (endTime - startTime)) + " KB/s");  //сделать меньше знаков после запятой
            }
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("Data stream IO Exce ption. Upload failed.", e);
        }
    }

    @Override
    public void download(String filename) throws ServiceException {
        try {
            Long startTime = new Date().getTime();

            DataInputStream dataInputStream = client.getDataInputStream();
            DataOutputStream dataOutputStream = client.getDataOutputStream();
            dataOutputStream.writeUTF("download");
            dataOutputStream.flush();
            dataOutputStream.writeUTF(filename);
            dataOutputStream.flush();

            if (!dataInputStream.readBoolean()) {
                System.out.println("No such file");
                return;
            }

            Long length = dataInputStream.readLong();
            FileOutputStream fileOutputStream = new FileOutputStream(BASKET_PATH + filename);
//                FileOutputStream fileOutputStream = new FileOutputStream("client/files/" + filename);

            int n;
            byte[] buf = new byte[4092];
            Long bytesRemaining = length;
            while (bytesRemaining != 0 && (n = dataInputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, n);
                fileOutputStream.flush();
                bytesRemaining -= n;
            }
            fileOutputStream.close();

            Long endTime = new Date().getTime();
            System.out.println("BITRATE: " + ((double) length / (endTime - startTime)) + " KB/s");
        } catch (IOException e) {
            throw new ServiceException("Data stream IO exception. Download failed", e);
        }
    }
}
