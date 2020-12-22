package com.yuditsky.socketapp.multithread.task;

import com.yuditsky.socketapp.entity.UdpServer;
import com.yuditsky.socketapp.service.impl.UdpServerService;
import lombok.Data;
import lombok.SneakyThrows;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Exchanger;

@Data
public class TimeTask extends Task {

    private Exchanger<byte[]> exchanger;
    private InetAddress address;
    private int port;
    private DatagramSocket socket;

    @SneakyThrows
    @Override
    public void run() {
        UdpServerService service = new UdpServerService(new UdpServer(), exchanger, socket, address, port);
        service.time();
    }
}
