package com.yuditsky.socketapp.controller.command.impl;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.service.ClientService;

import static com.yuditsky.socketapp.controller.util.Constant.PARAM_DELIMITER;

public class Connect implements Command {
    @Override
    public String execute(String request, ClientService clientService) {
        if (!clientService.isConnected()) {
            String ip = request.substring(0, request.indexOf(PARAM_DELIMITER));
            int port = Integer.parseInt(request.substring(request.indexOf(PARAM_DELIMITER) + 1));

            try {
                clientService.connect(ip, port);
                return "Connected";
            } catch (ServiceException e) {
                return e.getMessage();
            }
        } else {
            return "Already connected";
        }
    }
}
