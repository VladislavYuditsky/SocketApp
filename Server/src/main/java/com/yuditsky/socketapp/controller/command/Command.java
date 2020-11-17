package com.yuditsky.socketapp.controller.command;

import com.yuditsky.socketapp.service.ServerService;

public interface Command {
    void execute(String request, ServerService serverService);
}
