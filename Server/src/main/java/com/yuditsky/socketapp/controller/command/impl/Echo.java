package com.yuditsky.socketapp.controller.command.impl;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.exception.ServiceException;
import com.yuditsky.socketapp.service.ServerService;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class Echo implements Command {
    @Override
    public void execute(String request, ServerService serverService) {
        try {
            serverService.echo();
        } catch (ServiceException | IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
