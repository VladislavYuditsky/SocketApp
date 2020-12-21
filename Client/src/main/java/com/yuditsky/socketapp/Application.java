package com.yuditsky.socketapp;

import com.yuditsky.socketapp.controller.ClientController;
import com.yuditsky.socketapp.service.impl.TcpClientService;
import com.yuditsky.socketapp.service.impl.UdpClientService;
import com.yuditsky.socketapp.view.View;

public class Application {
    public static void main(String[] args) {
//        ClientController controller = new ClientController(new TcpClientService());
        ClientController controller = new ClientController(new UdpClientService());
        View view = new View(controller);
    }
}
