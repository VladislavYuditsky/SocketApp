package com.yuditsky.socketapp.controller.command.impl;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.service.ClientService;

public class WrongRequest implements Command {
    @Override
    public String execute(String request, ClientService clientService) {
        return "Wrong request";
    }
}
