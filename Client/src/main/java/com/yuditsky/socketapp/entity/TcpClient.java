package com.yuditsky.socketapp.entity;

import lombok.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

@Data
public class TcpClient extends Client {
    private Socket socket;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private String serverIp;
    private int serverPort;
}
