package com.yuditsky.socketapp.multithread;

import lombok.Data;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Optional;

@Data
public class CommonReader {

    private DatagramSocket socket;

    public CommonReader(DatagramSocket socket) {
        this.socket = socket;
    }

    public Optional<TransferData> receiveBytes() throws IOException {
        try {
            byte[] buf = new byte[65507];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            byte[] data = packet.getData();

            TransferData transferData = new TransferData();
            transferData.setAddress(address);
            transferData.setPort(port);
            transferData.setValue(data);
            transferData.setLength(packet.getLength());
            return Optional.of(transferData);
        } catch (Exception e){
            return Optional.empty();
        }
    }
}
