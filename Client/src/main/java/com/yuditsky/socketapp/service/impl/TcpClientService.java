package com.yuditsky.socketapp.service.impl;

import com.yuditsky.socketapp.entity.TcpClient;
import com.yuditsky.socketapp.exception.ConnectionException;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.exception.ValidationException;
import com.yuditsky.socketapp.handler.ConnectionHandler;
import com.yuditsky.socketapp.service.ClientService;
import com.yuditsky.socketapp.validator.impl.IpValidator;
import com.yuditsky.socketapp.validator.impl.PortValidator;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Log4j2
public class TcpClientService implements ClientService {

    private static final String BASKET_PATH = "basket/";

    private TcpClient client = new TcpClient();
    private IpValidator ipValidator = new IpValidator();
    private PortValidator portValidator = new PortValidator();
    private static Scanner inputScanner = new Scanner(System.in);

    int total = 0;

    @Override
    public void connect(String ip, int port) throws ValidationException, ConnectionException {
        ipValidator.validate(ip);
        portValidator.validate(port);

        try {
            Socket socket = new Socket(ip, port);
            socket.setKeepAlive(true);
            socket.setSoTimeout(2000);

            client.setSocket(socket);
            client.setDataInputStream(new DataInputStream(new BufferedInputStream(socket.getInputStream())));
            client.setDataOutputStream(new DataOutputStream(new BufferedOutputStream(socket.getOutputStream())));
            client.setConnected(true);

            client.setServerIp(ip);
            client.setServerPort(port);

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
        System.out.println("Upload");
        File file = new File(BASKET_PATH + filename);

        Long startTime = new Date().getTime();

        try {
            client.getDataOutputStream().writeUTF("upload");
            client.getDataOutputStream().flush();
            client.getDataOutputStream().writeUTF(file.getName());
            client.getDataOutputStream().flush();
            client.getDataOutputStream().writeLong(file.length());
            client.getDataOutputStream().flush();

            FileInputStream fileInputStream = new FileInputStream(file);
            int n;
            byte[] buf = new byte[65536];
            byte[] ack = new byte[1];
            while ((n = fileInputStream.read(buf)) != -1) {
                try {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Future future = executor.submit(new Task(buf, n));
                    future.get(3, TimeUnit.SECONDS);

                    client.getDataInputStream().read(ack);

                    total += n;

                } catch (Exception e) {

                    if (!waitReconnecting(client.getDataInputStream(), ack)) {
                        while (true) {
                            System.out.println("Try to reconnect? (y)");
                            if (inputScanner.next().equals("y")) {
                                try {
                                    connect(client.getServerIp(), client.getServerPort());
                                    client.getDataOutputStream().writeLong(total);
                                    client.getDataOutputStream().flush();
                                    break;
                                } catch (ValidationException | ConnectionException ex) {
                                    log.error("Failed");
                                }
                            } else {
                                close();
                                return;
                            }
                        }
                    }
                }
            }


            Long endTime = new Date().getTime();
            log.debug("BITRATE: " + ((double) file.length() / (endTime - startTime)) + " KB/s");
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("Data stream IO Exception. Upload failed.", e);
        }
    }

    @Override
    public boolean waitReconnecting(DataInputStream dataInputStream, byte[] ack) throws InterruptedException {
        long start = System.currentTimeMillis();
        long timeout = 15000L;
        long end = start + timeout;

        while (System.currentTimeMillis() < end) {
            Thread.sleep(1000);
            try {
                dataInputStream.read(ack);
                System.out.println("upload");
                return true;
            } catch (Exception e1) {
                log.debug("Wait");
            }
        }

        return false;
    }


    @Override
    public void download(String filename) throws ServiceException {
        System.out.println("Download");
        ConnectionHandler connectionHandler = new ConnectionHandler();

        int total1 = 0;

        try {
            Long startTime = new Date().getTime();

            client.getDataOutputStream().writeUTF("download");
            client.getDataOutputStream().flush();
            client.getDataOutputStream().writeUTF(filename);
            client.getDataOutputStream().flush();

            if (!client.getDataInputStream().readBoolean()) {
                log.debug("No such file");
                return;
            }

            Long length = client.getDataInputStream().readLong();
            FileOutputStream fileOutputStream = new FileOutputStream(BASKET_PATH + filename);

            int n;
            int portion = 0;
            int lastSubmittedPosition = 0;
            byte[] buf = new byte[65536];
            Long bytesRemaining = length;
            while (bytesRemaining != 0) {
                try {
                    n = client.getDataInputStream().read(buf);

                    portion += n;
                    fileOutputStream.write(buf, 0, n);
                    fileOutputStream.flush();
                    bytesRemaining -= n;
                    total1 += n;

                    if (portion == 65536 || bytesRemaining == 0) {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Future future = executor.submit(new Task1());
                        future.get(3, TimeUnit.SECONDS);
                        portion = 0;
                        lastSubmittedPosition = total1;
                    }

                    connectionHandler.setSecondsLeft(5);

                } catch (Exception e) {

                    if (connectionHandler.closeConnection()) {
                        fileOutputStream.getChannel().position(lastSubmittedPosition);
                        bytesRemaining += portion;
                        while (true) {
                            System.out.println("Try to reconnect? (y)");
                            if (inputScanner.next().equals("y")) {
                                try {
                                    connect(client.getServerIp(), client.getServerPort());
                                    client.getDataOutputStream().writeLong(lastSubmittedPosition);
                                    client.getDataOutputStream().flush();
                                    portion = 0;
                                    break;
                                } catch (ValidationException | ConnectionException ex) {
                                    log.error("Failed");
                                }
                            } else {
                                close();
                                fileOutputStream.close();
                                return;
                            }
                        }
                    }
                    continue;

                }
            }
            fileOutputStream.close();

            Long endTime = new Date().getTime();
            log.debug("BITRATE: " + ((double) length / (endTime - startTime)) + " KB/s");
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("Data stream IO exception. Download failed", e);
        }
    }

    class Task implements Runnable {
        byte[] buf;
        int n;

        public Task(byte[] buf, int n) {
            this.buf = buf;
            this.n = n;
        }

        @Override
        public void run() {
            try {
                client.getDataOutputStream().write(buf, 0, n);
                client.getDataOutputStream().flush();
            } catch (IOException e) {
            }
        }
    }

    class Task1 implements Runnable {
        @Override
        public void run() {
            try {
                client.getDataOutputStream().write(1);
                client.getDataOutputStream().flush();
            } catch (IOException e) {
            }
        }
    }
}
