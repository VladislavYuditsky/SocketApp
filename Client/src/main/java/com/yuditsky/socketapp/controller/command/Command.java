package com.yuditsky.socketapp.controller.command;

import com.yuditsky.socketapp.service.ClientService;

public interface Command {
    String execute(String request, ClientService clientService);
}
