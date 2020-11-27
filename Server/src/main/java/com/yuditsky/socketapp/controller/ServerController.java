package com.yuditsky.socketapp.controller;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.controller.command.CommandProvider;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.service.ServerService;
import lombok.extern.log4j.Log4j2;

import static com.yuditsky.socketapp.controller.util.Constant.PARAM_DELIMITER;

@Log4j2
public class ServerController {
    private CommandProvider commandProvider = new CommandProvider();
    private ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    public void execute() {
        try {
            serverService.init();
            serverService.connect();
            while (true) {
                String task = serverService.read();
                executeTask(task);
            }
        } catch (ServiceException e) {
            log.error(e.getMessage());
        }
    }

    public void executeTask(String request) {
        Command executionCommand;
        String commandName;

        try {
            commandName = request.substring(0, request.indexOf(PARAM_DELIMITER));
        } catch (StringIndexOutOfBoundsException e) {
            commandName = request;
        }

        executionCommand = commandProvider.getCommand(commandName);
        request = request.substring(request.indexOf(PARAM_DELIMITER) + 1);
        executionCommand.execute(request, serverService);
    }
}
