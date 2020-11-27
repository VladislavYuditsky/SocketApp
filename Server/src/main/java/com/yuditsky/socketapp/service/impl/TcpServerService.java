package com.yuditsky.socketapp.service.impl;

import com.yuditsky.socketapp.entity.TcpServer;
import com.yuditsky.socketapp.exception.ConnectionException;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.handler.ConnectionHandler;
import com.yuditsky.socketapp.service.ServerService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    public void connect() throws ServiceException {
        try {

            log.debug("Waiting");

            ServerSocket serverSocket = server.getServerSocket();

            Socket socket = serverSocket.accept();
            socket.setKeepAlive(true);
            socket.setSoTimeout(2000);

            server.setServerSocket(serverSocket);
            server.setSocket(socket);
            server.setDataInputStream(new DataInputStream(new BufferedInputStream(socket.getInputStream())));
            server.setDataOutputStream(new DataOutputStream(new BufferedOutputStream(socket.getOutputStream())));

            log.debug("Connected");

            System.out.println(server.getLastSocketAddress());
            System.out.println(socket.getRemoteSocketAddress());
            System.out.println(server.getLastIncompletedCommandName());

            if (server.getLastSocketAddress() != null && server.getLastSocketAddress().equals(socket.getInetAddress()) && server.getLastIncompletedCommandName() != null) {
                if (server.getLastIncompletedCommandName().equals("upload")) {
                    server.setOffset(server.getDataInputStream().readLong());
                    upload();
                } else {
                    download();
                }
            }

            server.setLastSocketAddress(socket.getInetAddress());

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
        server.setLastIncompletedCommandName("upload");
        int total = 0;

        System.out.println("OFFSET: " + server.getOffset());

        ConnectionHandler connectionHandler = new ConnectionHandler();
        try {

            if (server.getFileOutputStream() == null) {
                String filename = server.getDataInputStream().readUTF();
                long length = server.getDataInputStream().readLong();
                FileOutputStream os = new FileOutputStream(
                        STORE_PATH + filename
                );
                server.setFileOutputStream(os);
                server.setLength(length);
            }

            FileOutputStream fileOutputStream = server.getFileOutputStream();

            int lastSubmittedPosition = 0;
            int n = 0;
            int portion = 0;
            byte[] buf = new byte[65536];
            while (server.getLength() != 0) {
                try {
                    if ((n = server.getDataInputStream().read(buf)) == -1) {
                        break;
                    }
                    portion += n;
                    fileOutputStream.write(buf, 0, n);
                    fileOutputStream.flush();
                    total += n;
                    server.setLength(server.getLength() - n);
                    System.out.println("Осталось: " + server.getLength() + " n = " + n);

                    if (portion == 65536 || server.getLength() == 0) {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Future future = executor.submit(new Task(server.getDataOutputStream()));
                        future.get(3, TimeUnit.SECONDS);
                        portion = 0;
                        lastSubmittedPosition = total;
                    }

                    connectionHandler.setSecondsLeft(10);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    if (connectionHandler.closeConnection()) {
                        Long position = fileOutputStream.getChannel().position();
                        fileOutputStream.getChannel().position(lastSubmittedPosition);
                        System.out.println("POSITION " + position);
                        System.out.println("CUR TOTAL: " + total);
                        close();
                        return;
                    }
                    continue;
                }
            }

            fileOutputStream.close();
            server.setFileOutputStream(null);
            server.setLength(0L);
            server.setLastIncompletedCommandName(null);
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("Data stream IO exception. Upload failed", e);
        }
    }

    @Override
    public void download() throws ServiceException {
        server.setLastIncompletedCommandName("download");
        try {
            try {
                if (server.getFileInputStream() == null) {
                    String filename = server.getDataInputStream().readUTF();
                    File file = new File(STORE_PATH + filename);
                    System.out.println(filename);
                    server.getDataOutputStream().writeBoolean(true);
                    server.getDataOutputStream().flush();

                    System.out.println(file.length());
                    server.getDataOutputStream().writeLong(file.length());
                    server.getDataOutputStream().flush();

                    FileInputStream fileInputStream = new FileInputStream(file);

                    server.setFileInputStream(fileInputStream);
                } else {
                    Long position = server.getDataInputStream().readLong();
                    System.out.println("TO THIS POSITION: " + position);
                    server.getFileInputStream().getChannel().position(position);
                }

                int n;
                byte[] buf = new byte[65536];
                byte[] ack = new byte[1];
                while ((n = server.getFileInputStream().read(buf)) != -1) {
                    try {
                        System.out.println(n);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Future future = executor.submit(new Task1(buf, n));
                        future.get(3, TimeUnit.SECONDS);

                        server.getDataInputStream().read(ack);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        if (!waitReconnecting(server.getDataInputStream(), ack)) {
                            close();
                        }
                    }
                }

            } catch (Exception e) {
                server.getDataOutputStream().writeBoolean(false);
                server.getDataOutputStream().flush();
                return;
            }

            server.getFileInputStream().close();
            server.setFileInputStream(null);
            server.setLastIncompletedCommandName(null);

        } catch (IOException e) {
            throw new ServiceException("Data stream IO exception. Download failed.", e);
        }
    }

    private boolean waitReconnecting(DataInputStream dataInputStream, byte[] ack) throws InterruptedException {
        long start = System.currentTimeMillis();
        long timeout = 10000L;
        long end = start + timeout;

        while (System.currentTimeMillis() < end) {
            Thread.sleep(1000);
            try {
                dataInputStream.read(ack);
                return true;
            } catch (Exception e1) {
                System.out.println("wait");
            }
        }

        return false;
    }

    class Task implements Runnable {
        DataOutputStream dataOutputStream;

        public Task(DataOutputStream dataOutputStream) {
            this.dataOutputStream = dataOutputStream;
        }

        @Override
        public void run() {
            try {
                server.getDataOutputStream().write(1);
                server.getDataOutputStream().flush();
            } catch (IOException e) {
            }
        }
    }

    class Task1 implements Runnable {

        byte[] buf;
        int n;

        public Task1(byte[] buf, int n) {
            this.buf = buf;
            this.n = n;
        }

        @Override
        public void run() {
            try {
                server.getDataOutputStream().write(buf, 0, n);
                server.getDataOutputStream().flush();
            } catch (IOException e) {
            }
        }
    }
}
