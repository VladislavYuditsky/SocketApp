package com.yuditsky.socketapp.multithread.task;

import lombok.Data;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Exchanger;

@Data
public abstract class Task implements Runnable {

    private Exchanger<byte[]> exchanger;
    private InetAddress address;
    private int port;
    private DatagramSocket socket;

}
