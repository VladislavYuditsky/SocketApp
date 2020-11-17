package com.yuditsky.socketapp.controller.command.impl;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.service.ServerService;
import com.yuditsky.socketapp.exception.ServiceException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Close implements Command {
    @Override
    public void execute(String request, ServerService serverService) {
        try {
            serverService.close();
//            serverService.connect();
        } catch (ServiceException e) {
            log.error("Can't close connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
