package com.yuditsky.socketapp;

import com.yuditsky.socketapp.controller.ServerController;
import com.yuditsky.socketapp.entity.TcpServer;
import com.yuditsky.socketapp.service.impl.TcpServerService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Application {
    public static void main(String[] args) {
        int port = 1234;
        TcpServer server = new TcpServer(port);
        log.info("Server port: " + port);

        ServerController controller = new ServerController(new TcpServerService(server));
        controller.execute();
    }
}
