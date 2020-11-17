package com.yuditsky.socketapp.controller.command.impl;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.service.ServerService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Connect implements Command {
    @Override
    public void execute(String request, ServerService serverService) {
//        int port = Integer.parseInt(request); зачем вообще его проверять? Удалить валидатор
        try {
            serverService.connect();
            log.debug("Connected");
        } catch (ServiceException e) {
            log.error("Connection failed: " + e.getMessage());
        }
    }
}
