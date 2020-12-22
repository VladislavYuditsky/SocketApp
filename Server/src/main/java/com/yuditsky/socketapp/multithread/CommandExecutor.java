package com.yuditsky.socketapp.multithread;

import com.yuditsky.socketapp.multithread.parser.Parser;
import com.yuditsky.socketapp.multithread.task.Task;
import com.yuditsky.socketapp.multithread.task.TaskProvider;
import lombok.SneakyThrows;

import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Exchanger;

public class CommandExecutor implements Runnable {

    private DatagramSocket socket;
    private ConnectionPool connectionPool;
    private CommonReader reader;
    private Parser parser;
    private String commandName;
    private TaskProvider provider;

    @SneakyThrows
    public CommandExecutor(Parser parser, int port, String commandName) {
        socket = new DatagramSocket(port);
        socket.setSoTimeout(1000);
        connectionPool = new ConnectionPool();
        reader = new CommonReader(socket);
        this.parser = parser;
        this.commandName = commandName;
        provider = new TaskProvider();
    }

    @SneakyThrows
    @Override
    public void run() {
        while (true) {
            Optional<TransferData> optional = reader.receiveBytes();

            if (optional.isPresent()) {
                TransferData data = optional.get();
                if (!connectionPool.exist(data.getKey())) {
                    Exchanger<byte[]> exchanger = connectionPool.create(data.getKey());
                    startTask(data, exchanger);
                } else {
                    if (parser.checkCommand(data)) {
                        Exchanger<byte[]> exchanger = connectionPool.replays(data.getKey());
                        startTask(data, exchanger);
                    } else {
                        Exchanger<byte[]> exchanger = connectionPool.get(data.getKey());
                        exchanger.exchange(Arrays.copyOfRange(data.getValue(), 0, Math.toIntExact(data.getLength())));
                    }
                }
            }
        }
    }

    private void startTask(TransferData data, Exchanger exchanger) {
        Task task = provider.get(commandName);
        task.setAddress(data.getAddress());
        task.setExchanger(exchanger);
        task.setPort(data.getPort());
        task.setSocket(socket);
        new Thread(task).start();
    }
}
