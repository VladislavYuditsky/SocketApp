package com.yuditsky.socketapp.service.impl;

import com.yuditsky.socketapp.entity.UdpServer;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.service.ServerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

public class UdpServerService implements ServerService {
    private static final String STORE_PATH = "store/";

    private final DatagramSocket socket;
    private byte[] buf = new byte[65507];
//    private DatagramPacket packet;
    private final InetAddress address;
    private final int port;

    private UdpServer server;

    private int x = 0;
    List<Integer> packetNumbers = new ArrayList<>();
    String filename;
    Long fileSize;
    byte[] bfile;

    private int d1 = 0;
    private FileInputStream fileInputStream;

    private Exchanger<byte[]> exchanger;

//    public UdpServerService(UdpServer server, int port) {
//        try {
//            this.server = server;
//            socket = new DatagramSocket(port);
////            socket.setSoTimeout(150);
//            socket.setSoTimeout(1000);
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//    }

    public UdpServerService(UdpServer server, Exchanger<byte[]> exchanger, DatagramSocket socket,
                            InetAddress address, int port){
        this.server = server;
        this.exchanger = exchanger;
        this.socket = socket;
        this.port = port;
        this.address = address;
    }

    @Override
    public void echo() throws IOException, InterruptedException {
//        String received = receiveString();
        String received = new String(exchanger.exchange("1".getBytes()));


        System.out.println(received);
        buf = received.toUpperCase().getBytes();

//        address = packet.getAddress();
//        int port = packet.getPort();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    @Override
    public void time() throws IOException {
        buf = new Date().toString().getBytes();
        System.out.println(new Date().toString());
//        address = packet.getAddress();
//        int port = packet.getPort();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    @Override
    public void close() {

    }

    @Override
    public void init() throws ServiceException {

    }

    @Override
    public void upload() throws IOException, InterruptedException {
        byte[] m = new byte[]{1};

        long total = 0L;

        if (packetNumbers.size() > 0) {
            x = 0;
            fun(packetNumbers, filename, fileSize, bfile);
            try (FileOutputStream fos = new FileOutputStream(STORE_PATH + "1" + filename)) {
                fos.write(bfile, 0, Math.toIntExact(fileSize));
                fos.flush();
            }
            sendString("done");
            return;
        }

//        filename = receiveString();
        filename = new String(exchanger.exchange(m));


//        Long length = bytesToLong(receiveBytes());
        Long length = ByteBuffer.wrap(exchanger.exchange(m)).getLong();


        System.out.println("LENGTH: " + length);
        fileSize = length;
        System.out.println("FILENAME " + filename);
//        FileOutputStream fileOutputStream = new FileOutputStream(STORE_PATH + filename);

        packetNumbers = new ArrayList<>();
        for (Integer i = 0; i <= length / 65407; i++) {
            packetNumbers.add(i);
        }

        bfile = new byte[Math.toIntExact(fileSize)];

        System.out.println(packetNumbers);

        int n = 0;
        buf = new byte[65507];
        while (length != 0) {
            Integer packetNumber = 0;
            try {
//                System.out.println("R: " + receiveBytes().length);
//                n = packet.getLength();

                buf = exchanger.exchange(m, 1000, TimeUnit.MILLISECONDS);
                n = buf.length;

                byte[] sHeader = new byte[100];
                System.arraycopy(buf, 0, sHeader, 0, 100);
                String sHeaderStr = new String(sHeader);
                String[] strs = sHeaderStr.split("~");
                packetNumber = Integer.valueOf(strs[1]);
                System.out.println("PACKET NUMBER " + packetNumber);

                buf = Arrays.copyOfRange(buf, 100, n);

            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println(e);

                x = 0;
                fun(packetNumbers, filename, fileSize, bfile);

                break;
            }
            System.arraycopy(buf, 0, bfile, packetNumber * 65407, buf.length);
//            fileOutputStream.write(buf, 0, n - 100);
//            fileOutputStream.flush();
            length -= (n - 100);
            total += (n - 100);
            System.out.println("TOTAL: " + total);
            packetNumbers.remove(packetNumber);
            System.out.println(packetNumbers);
        }

        try (FileOutputStream fos = new FileOutputStream(STORE_PATH + "1" + filename)) {
            fos.write(bfile, 0, Math.toIntExact(fileSize));
            fos.flush();
        }

//        fileOutputStream.close();

        sendString("done");

        System.out.println(packetNumbers);
        System.out.println(fileSize);
    }

    private void fun(List<Integer> packetNumbers, String filename, long fileSize, byte[] bfile) throws IOException {

        sendString("~~" + packetNumbers.size());

        for (int i = 0; i < packetNumbers.size(); i++) {
            sendString(String.valueOf(packetNumbers.get(i)));
        }


        int k = packetNumbers.size();

        for (int i = 0; i < k; i++) {

            try {
//                receiveBytes();
                buf = exchanger.exchange("1".getBytes(), 1000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                x++;
                if (x == 50) {
                    System.out.println("Connection lost");
                    return;
                }
                fun(packetNumbers, filename, fileSize, bfile);
                return;
            }
//            int n = packet.getLength();
            int n = buf.length;
            byte[] sHeader = new byte[100];
            System.arraycopy(buf, 0, sHeader, 0, 100);
            String sHeaderStr = new String(sHeader);
            String[] strs = sHeaderStr.split("~");
            Integer packetNumber = Integer.valueOf(strs[1]);
            buf = Arrays.copyOfRange(buf, 100, n);

            System.arraycopy(buf, 0, bfile, packetNumber * 65407, buf.length);

            packetNumbers.remove(packetNumber);

            System.out.println(packetNumbers);
        }

        if (!packetNumbers.isEmpty()) {
            fun(packetNumbers, filename, fileSize, bfile);
        }
    }

    @Override
    public void download() throws IOException, InterruptedException {

        if (d1 == 15) {
            d1 = 0;
            downloadLoss(fileInputStream);
            return;
        }

//        String filename = receiveString();
        String filename = new String(exchanger.exchange("1".getBytes()));

        System.out.println("FILENAME " + filename);
        File file;
        try {
            file = new File(STORE_PATH + filename);
            sendBytes(longToBytes(1));
            System.out.println("SEND 1");
        } catch (Exception e) {
            sendBytes(longToBytes(0));
            System.out.println("SEND 0 AND RETURN");
            return;
        }
        long length = file.length();
        sendBytes(longToBytes(length));
        System.out.println("SEND FILE LENGTH " + length);

        fileInputStream = new FileInputStream(file);
        int n;
        long total = 0L;
        buf = new byte[65407];
        while ((n = fileInputStream.read(buf)) != -1) {
            buf = Arrays.copyOfRange(buf, 0, n);

            Integer i = Math.toIntExact((total / 65407));
            System.out.println("PACKET NUMBER " + i);
            String header = "~" + i + "~";
            byte[] packetHeader = Arrays.copyOfRange(header.getBytes(), 0, header.getBytes().length);
            byte[] packet = new byte[100 + n];
            System.arraycopy(packetHeader, 0, packet, 0, packetHeader.length);
            System.arraycopy(buf, 0, packet, 100, buf.length);
            buf = packet;

            sendBytes(buf);
            total += n;
            buf = new byte[65407];
        }

        downloadLoss(fileInputStream);

    }

    private void downloadLoss(FileInputStream fileInputStream) throws IOException, InterruptedException {

        String str = "";

        try {
//            str = receiveString();
            str = new String(exchanger.exchange("1".getBytes()));

            System.out.println(str);
        } catch (Exception e) {
            d1++;
            if (d1 == 15) {
                System.out.println("Connection lost");
                return;
            } else {
                downloadLoss(fileInputStream);
                return;
            }
        }

        d1 = 0;

        if (str.equals("done")) {
            System.out.println("Done");
            return;
        } else {
            int lossPacketNumber = -1;
            if (str.startsWith("1~")) {
                try {
                    str = str.replaceFirst("1~", "");
                    lossPacketNumber = Integer.parseInt(str);
                } catch (Exception e) {
                    downloadLoss(fileInputStream);
                    return;
                }
                List<Integer> lossPackets = new ArrayList<>();
                for (int i = 0; i < lossPacketNumber; i++) {
                    String lossPacketStr = "";
                    try {
//                        lossPacketStr = receiveString();
                        lossPacketStr = new String(exchanger.exchange("1".getBytes()));
                    } catch (Exception e) {
                        downloadLoss(fileInputStream);
                        return;
                    }
                    if (lossPacketStr.equals("done")) {
                        return;
                    }
                    Integer lossPacket = -1;
                    try {
                        lossPacket = Integer.parseInt(lossPacketStr);
                        if (!lossPackets.contains(lossPacketNumber)) {
                            lossPackets.add(lossPacket);
                        }
                    } catch (NumberFormatException e) {
                    }
                }

                for (int i = 0; i < lossPackets.size(); i++) {
                    System.out.println("I " + i);
                    buf = new byte[65407];
                    Integer lossPacketNum = lossPackets.get(i);
                    String lossPacketStr = String.valueOf(lossPacketNum);
                    fileInputStream.getChannel().position(65407 * lossPacketNum);
                    int n = fileInputStream.read(buf);
                    String header = "~" + lossPacketNum + "~";
                    byte[] buf1 = Arrays.copyOfRange(header.getBytes(), 0, header.getBytes().length);
                    byte[] buf2 = Arrays.copyOfRange(buf, 0, n);
                    byte[] buf3 = new byte[100 + n];
                    System.arraycopy(buf1, 0, buf3, 0, buf1.length);
                    System.arraycopy(buf2, 0, buf3, 100, buf2.length);
                    buf = buf3;
                    System.out.println("SEND");
                    sendBytes(buf);
//                Thread.sleep(7);
                }
            }

            downloadLoss(fileInputStream);

        }
    }

    @Override
    public String read() throws ServiceException {
//        try {
//            String command = receiveString();
////            System.out.println("UDP " + command);
//            if (command.startsWith("~") && packetNumbers.size() != 0) {
//                return "upload";
//            }
//            if (command.startsWith("1~")) {
//                return "download";
//            }
//            return command;
//        } catch (IOException e) {
//            return "";
//        }
        return "";
    }

    @Override
    public void connect() {

    }

    private void sendBytes(byte[] bytes) throws IOException {
        buf = bytes;
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    private void sendString(String message) throws IOException {
        buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

//    private String receiveString() throws IOException {
//        buf = new byte[256];
//        packet = new DatagramPacket(buf, buf.length);
//        socket.receive(packet);
//        address = packet.getAddress();
//        port = packet.getPort();
//        return new String(packet.getData(), 0, packet.getLength());
//    }
//
//    private byte[] receiveBytes() throws IOException {
//        buf = new byte[65507];
//        packet = new DatagramPacket(buf, buf.length);
//        socket.receive(packet);
//        address = packet.getAddress();
//        port = packet.getPort();
//        return packet.getData();
//    }

    private byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    private long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        byte[] d = Arrays.copyOfRange(bytes, 0, 8);
        buffer.put(d);
        buffer.flip();//need flip
        return buffer.getLong();
    }
}
