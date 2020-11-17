package com.yuditsky.socketapp.controller.command.impl;

import com.yuditsky.socketapp.controller.command.Command;
import com.yuditsky.socketapp.service.ServerService;

public class WrongRequest implements Command {
    @Override
    public void execute(String request, ServerService serverService) {
    }
}
