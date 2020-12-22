package com.yuditsky.socketapp;

import com.yuditsky.socketapp.entity.UdpServer;
import com.yuditsky.socketapp.multithread.CommandExecutor;
import com.yuditsky.socketapp.multithread.parser.DownloadParser;
import com.yuditsky.socketapp.multithread.parser.EchoParser;
import com.yuditsky.socketapp.multithread.parser.TimeParser;
import com.yuditsky.socketapp.multithread.parser.UploadParser;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Application {
    public static void main(String[] args) {
//        int port = 1234;
//        TcpServer server = new TcpServer(port);
        UdpServer server = new UdpServer();
//        log.info("Server port: " + port);

//        Exchanger<String> exchanger = new Exchanger<>();

//        ServerService serverServiceEcho = new UdpServerService(new UdpServer(), 1111);
//        ServerService serverServiceTime = new UdpServerService(new UdpServer(), 2222);
//        ServerService serverServiceDownload = new UdpServerService(new UdpServer(), 3333);
//        ServerService serverServiceUpload = new UdpServerService(new UdpServer(), 4444);


//        ServerController controller = new ServerController(serverService, exchanger);
        CommandExecutor echoExecutor = new CommandExecutor(new EchoParser(), 1111, "echo");
        CommandExecutor timeExecutor = new CommandExecutor(new TimeParser(), 2222, "time");
        CommandExecutor uploadExecutor = new CommandExecutor(new UploadParser(), 3333, "upload");
        CommandExecutor downloadExecutor = new CommandExecutor(new DownloadParser(), 4444, "download");

//        ServerController controller = new ServerController(new TcpServerService(server));
//        new Thread(controller).start();
        new Thread(echoExecutor).start();
        new Thread(timeExecutor).start();
        new Thread(uploadExecutor).start();
        new Thread(downloadExecutor).start();
    }
}
