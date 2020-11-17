package com.yuditsky.socketapp.controller.command.impl;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.service.ClientService;
import com.yuditsky.socketapp.exception.ServiceException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Close implements Command {
    @Override
    public String execute(String request, ClientService clientService) {
        try {
            clientService.close();
            log.debug("Connection closed");
            return "Connection closed";
        } catch (ServiceException e) {
            log.error("Close command failed: " + e.getMessage());
            return "Can't close connection";
        }
    }
}
