package com.yuditsky.socketapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcpServer extends Server {
    private int port;

    private ServerSocket serverSocket;
    private Socket socket;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private InetAddress lastSocketAddress;
    private String lastIncompletedCommandName;
    private FileOutputStream fileOutputStream;

    private Long length;
    private Long offset;

    private FileInputStream fileInputStream;

    public TcpServer(int port) {
        this.port = port;
    }
}
