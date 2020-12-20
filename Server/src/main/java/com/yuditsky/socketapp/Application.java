package com.yuditsky.socketapp;

import com.yuditsky.socketapp.controller.ServerController;
import com.yuditsky.socketapp.entity.UdpServer;
import com.yuditsky.socketapp.service.impl.UdpServerService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Application {
    public static void main(String[] args) {
        int port = 1234;
//        TcpServer server = new TcpServer(port);
        UdpServer server = new UdpServer();
        log.info("Server port: " + port);

        ServerController controller = new ServerController(new UdpServerService(server));
//        ServerController controller = new ServerController(new TcpServerService(server));
        controller.execute();
    }
}
