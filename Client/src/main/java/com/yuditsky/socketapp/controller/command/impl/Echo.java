package com.yuditsky.socketapp.controller.command.impl;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.service.ClientService;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class Echo implements Command {
    @Override
    public String execute(String request, ClientService clientService) {
        try {
            return clientService.echo(request);
        } catch (ServiceException | IOException e) {
            log.error(e);
            return e.getMessage();
        }
    }
}
