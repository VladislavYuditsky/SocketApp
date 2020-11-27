package com.yuditsky.socketapp.controller;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.controller.command.CommandProvider;
import com.yuditsky.socketapp.service.ClientService;

import static com.yuditsky.socketapp.controller.util.Constant.PARAM_DELIMITER;

public class ClientController {
    private ClientService clientService;
    private CommandProvider commandProvider = new CommandProvider();

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    public String executeTask(String request) {
        Command executionCommand;
        String commandName;

        try {
            commandName = request.substring(0, request.indexOf(PARAM_DELIMITER));
        } catch (StringIndexOutOfBoundsException e) {
            commandName = request;
        }

        executionCommand = commandProvider.getCommand(commandName);
        request = request.substring(request.indexOf(PARAM_DELIMITER) + 1);
        return executionCommand.execute(request, clientService);
    }
}
