package com.yuditsky.socketapp.controller.command.impl;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.service.ClientService;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class Download implements Command {
    @Override
    public String execute(String request, ClientService clientService) {
        try {
            clientService.download(request);
            return "Downloading ended.";
        } catch (ServiceException | IOException | InterruptedException e) {
            log.error(e);
            return e.getMessage();
        }
    }
}
