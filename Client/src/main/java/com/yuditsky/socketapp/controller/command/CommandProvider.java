package com.yuditsky.socketapp.controller.command;

import com.yuditsky.socketapp.controller.command.impl.*;

import java.util.HashMap;
import java.util.Map;

public class CommandProvider {
    private final Map<CommandName, Command> repository = new HashMap<>();

    public CommandProvider(){
        repository.put(CommandName.CONNECT, new Connect());
        repository.put(CommandName.CLOSE, new Close());
        repository.put(CommandName.ECHO, new Echo());
        repository.put(CommandName.DOWNLOAD, new Download());
        repository.put(CommandName.UPLOAD, new Upload());
        repository.put(CommandName.TIME, new Time());
        repository.put(CommandName.WRONG_REQUEST, new WrongRequest());
    }

    public Command getCommand(String name){
        CommandName commandName;
        Command command;

        try{
            commandName = CommandName.valueOf(name.toUpperCase());
            command = repository.get(commandName);
        } catch (IllegalArgumentException e) {
            command = repository.get(CommandName.WRONG_REQUEST);
        }

        return command;
    }
}
