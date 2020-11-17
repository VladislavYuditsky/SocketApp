package com.yuditsky.socketapp.controller.command.impl;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.service.ClientService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Upload implements Command {
    @Override
    public String execute(String request, ClientService clientService) {
        try {
            clientService.upload(request);
            return "Uploaded";
        } catch (ServiceException e) {
            log.error(e);
            return e.getMessage();
        }
    }
}
