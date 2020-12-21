package com.yuditsky.socketapp.service.impl;

import com.yuditsky.socketapp.exception.ConnectionException;
import com.yuditsky.socketapp.exception.ValidationException;
import com.yuditsky.socketapp.service.ClientService;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

@Log4j2
public class UdpClientService implements ClientService {

    private static final String BASKET_PATH = "basket/";

    private DatagramSocket socket;
    private DatagramPacket packet;
    private InetAddress address;
    private int port = 1234;

    private byte[] buf = new byte[65507];
    private static final Scanner inputScanner = new Scanner(System.in);

    private long total = 0L;

    private long x = 0L;
    private long y = 0L;

    private long d1 = 0L;

    byte[] bfile;
    List<Integer> packetNumbers = new ArrayList<>();

    public UdpClientService() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("192.168.100.4");
            socket.setSoTimeout(1100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String echo(String message) throws IOException {
        sendString("echo");
        sendString(message);
//        System.out.println(receiveString());
        return receiveString();
    }

    @Override
    public String time() throws IOException {
        sendString("time");
//        System.out.println(receiveString());
        return receiveString();
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void close() {
        socket.close();
    }

    @Override
    public boolean waitReconnecting(DataInputStream dataInputStream, byte[] ack) throws InterruptedException {
        return false;
    }

    @Override
    public void upload(String filename) throws IOException, InterruptedException {
        x = 0L;
        y = 0L;

        long total = 0L;

        File file = new File(BASKET_PATH + filename);

        System.out.println("FILE SIZE: " + file.length());

        Long startTime = new Date().getTime();

        sendString("upload");
        sendString(file.getName());
        sendBytes(longToBytes(file.length()));


        List<Integer> packetNumbers = new ArrayList<>();
        for (Integer i = 0; i <= file.length() / 65407; i++) {
            packetNumbers.add(i);
        }


        FileInputStream fileInputStream = new FileInputStream(file);
        int n;
        buf = new byte[65407];
        while ((n = fileInputStream.read(buf)) != -1) {
            try {

                Integer i = (int) (total / 65407);

                String header = "~" + i + "~";

                byte[] buf1;
                buf1 = Arrays.copyOfRange(header.getBytes(), 0, header.getBytes().length);


                byte[] buf2;
                buf2 = Arrays.copyOfRange(buf, 0, n);


                byte[] buf3 = new byte[100 + n];
                System.arraycopy(buf1, 0, buf3, 0, buf1.length);
                System.arraycopy(buf2, 0, buf3, 100, buf2.length);


                buf = buf3;

                System.out.println(buf[0]);
                sendBytes(buf);
                System.out.println("N: " + (n + 100));
                total += n;
                System.out.println("TOTAL: " + total);

                packetNumbers.remove(i);

                Thread.sleep(7);
                if (y != 0) {
                    System.out.println("Connected");
                }
                x = 0L;
                y = 0L;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Long position = fileInputStream.getChannel().position();
                fileInputStream.getChannel().position(position - n);
                buf = new byte[65407];
                Thread.sleep(100);
                if (x == 150) {
                    System.out.println("Try to reconnect? y/n");
                    if (inputScanner.next().equals("y")) {
                        x++;
                        System.out.println("Wait");
                        continue;
                    } else {
                        return;
                    }
                }
                if (x == 151) {
                    y++;
                    if (y == 20) {
                        y = 0L;
                        x = 20;
                    }
                    continue;
                }
                x++;
            }
            buf = new byte[65407];
        }

        System.out.println(packetNumbers);

        fun(file, startTime, fileInputStream);
    }

    private void fun(File file, Long startTime, FileInputStream fileInputStream) throws IOException, InterruptedException {
        String str = "-1";
        try {
            str = receiveString();
        } catch (Exception e){
            fun(file, startTime, fileInputStream);
            return;
        }

        if (str.equals("done")) {
            Long endTime = new Date().getTime();
            System.out.println("BITRATE: " + (file.length() / (endTime - startTime)) + " KB/s");
            return;
        } else {
            if (str.startsWith("~~")) {
                str = str.replaceFirst("~~", "");
                int lossPacketNumber = Integer.parseInt(str);
                List<Integer> lossPackets = new ArrayList<>();
                for (int i = 0; i < lossPacketNumber; i++) {
                    String lossPacketStr = receiveString();
                    if (lossPacketStr.equals("done")) {
                        return;
                    }
                    if (!lossPacketStr.startsWith("~~")) {
                        Integer lossPacketNum = Integer.parseInt(lossPacketStr);
                        if (!lossPackets.contains(lossPacketNum)) {
                            lossPackets.add(lossPacketNum);
                        }
                    }
                }

                for (int i = 0; i < lossPackets.size(); i++) {
                    buf = new byte[65407];
                    Integer lossPacketNum = lossPackets.get(i);
                    String lossPacketStr = String.valueOf(lossPacketNum);
                    fileInputStream.getChannel().position(65407 * lossPacketNum);
                    int n = fileInputStream.read(buf);
                    String header = "~" + lossPacketStr + "~";
                    byte[] buf1 = Arrays.copyOfRange(header.getBytes(), 0, header.getBytes().length);
                    byte[] buf2 = Arrays.copyOfRange(buf, 0, n);
                    byte[] buf3 = new byte[100 + n];
                    System.arraycopy(buf1, 0, buf3, 0, buf1.length);
                    System.arraycopy(buf2, 0, buf3, 100, buf2.length);
                    buf = buf3;
                    sendBytes(buf);
                    Thread.sleep(7);
                }
            }

            fun(file, startTime, fileInputStream);
        }
    }

    @Override
    public void download(String filename) throws IOException, InterruptedException {
        Long startTime = new Date().getTime();

        sendString("download");
        sendString(filename);

        if (bytesToLong(receiveBytes()) == 0) {
            System.out.println("No such file");
            return;
        }

        Long length = bytesToLong(receiveBytes());
        System.out.println("LENGTH " + length);

        for (int i = 0; i < length / 65407; i++) {
            packetNumbers.add(i);
        }

        if (length % 65407 != 0) {
            packetNumbers.add((int) (length / 65407));
            System.out.println("LAST PACKET NUMBER " + (int) (length / 65407));
        }

        int n;
        buf = new byte[65507];
        Long bytesRemaining = length;
        System.out.println("REMAINING" + bytesRemaining);
        bfile = new byte[Math.toIntExact(length)];
        while (bytesRemaining != 0) {
            try {
                receiveBytes();
            } catch (Exception e) {
                downloadLoss(filename, length, startTime);
                return;
            }
            n = packet.getLength();
            System.out.println("N " + n);
            byte[] header = new byte[100];
            System.arraycopy(buf, 0, header, 0, 100);
            String headerStr = new String(header);
            String[] strs = headerStr.split("~");
            Integer packetNumber = Integer.valueOf(strs[1]);
            buf = Arrays.copyOfRange(buf, 100, n);
            System.arraycopy(buf, 0, bfile, packetNumber * 65407, buf.length);

            packetNumbers.remove(packetNumber);

            bytesRemaining -= (n - 100);
            System.out.println("BYTES REMAINING " + bytesRemaining);
            System.out.println(packetNumbers);

        }

        System.out.println(packetNumbers);

        try (FileOutputStream fileOutputStream = new FileOutputStream(BASKET_PATH + filename)) {
            fileOutputStream.write(bfile, 0, Math.toIntExact(length));
            fileOutputStream.flush();
        }

        Long endTime = new Date().getTime();
        System.out.println("BITRATE: " + (length / (endTime - startTime)) + " KB/s");

        sendString("done");
    }

    private void downloadLoss(String filename, Long length, Long startTime) throws IOException, InterruptedException {

        System.out.println("PACKET NUMBERS " + packetNumbers.size());
        try {
            sendString("1~" + packetNumbers.size());
            for (int i = 0; i < packetNumbers.size(); i++) {
                sendString(String.valueOf(packetNumbers.get(i)));
                Thread.sleep(3);
            }
        }catch (IOException | InterruptedException e){
            d1++;
            Thread.sleep(100);
            if(d1 == 150){
                d1 = 0L;
                System.out.println("Connection lost. Try to reconnect? y/n");
                if (inputScanner.next().equals("y")) {
                    System.out.println("Wait");
                    downloadLoss(filename, length, startTime);
                }
                return;
            }
            downloadLoss(filename, length, startTime);
            return;
        }

        d1 = 0;

        int k = packetNumbers.size();

        for (int i = 0; i < k; i++) {
            try {
                receiveBytes();
            } catch (Exception e) {
                System.out.println(e);
                System.out.println(e.getMessage());
                downloadLoss(filename, length, startTime);
                return;
            }
            int n = packet.getLength();

            byte[] header = new byte[100];
            System.arraycopy(buf, 0, header, 0, 100);
            String headerStr = new String(header);
            if(headerStr.startsWith("~")) {
                String[] strs = headerStr.split("~");
                Integer packetNumber = Integer.valueOf(strs[1]);
                buf = Arrays.copyOfRange(buf, 100, n);
                System.arraycopy(buf, 0, bfile, packetNumber * 65407, buf.length);
                packetNumbers.remove(packetNumber);
                System.out.println(packetNumbers);
            }
        }

        if (!packetNumbers.isEmpty()) {
            downloadLoss(filename, length, startTime);
            return;
        }

        sendString("done");

        Long endTime = new Date().getTime();
        System.out.println("BITRATE: " + (length / (endTime - startTime)) + " KB/s");

        try (FileOutputStream fileOutputStream = new FileOutputStream(BASKET_PATH + filename)) {
            fileOutputStream.write(bfile, 0, Math.toIntExact(length));
            fileOutputStream.flush();
        }

    }

    @Override
    public void connect(String ip, int port) throws ValidationException, ConnectionException {

    }

    private void sendBytes(byte[] bytes) throws IOException {
        buf = bytes;
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    private void sendString(String message) throws IOException {
        buf = message.getBytes();
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    private String receiveString() throws IOException {
        buf = new byte[256];
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        address = packet.getAddress();
        port = packet.getPort();
        return new String(packet.getData(), 0, packet.getLength());
    }

    private byte[] receiveBytes() throws IOException {
        buf = new byte[65507];
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        address = packet.getAddress();
        port = packet.getPort();
        return packet.getData();
    }

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
