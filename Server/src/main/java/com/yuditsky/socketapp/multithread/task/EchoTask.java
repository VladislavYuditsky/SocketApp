package com.yuditsky.socketapp.multithread.task;

import com.yuditsky.socketapp.entity.UdpServer;
import com.yuditsky.socketapp.service.impl.UdpServerService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Exchanger;

@Data
@NoArgsConstructor
class EchoTask extends Task implements Runnable {

    private Exchanger<byte[]> exchanger;
    private InetAddress address;
    private int port;
    private DatagramSocket socket;

    @SneakyThrows
    @Override
    public void run() {
        UdpServerService service = new UdpServerService(new UdpServer(), exchanger, socket, address, port);
        service.echo();
    }
}
