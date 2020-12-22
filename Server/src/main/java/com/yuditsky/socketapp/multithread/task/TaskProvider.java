package com.yuditsky.socketapp.multithread.task;

public class TaskProvider {
    public Task get(String commandName) {
        switch (commandName) {
            case "download":
                return new DownloadTask();
            case "time":
                return new TimeTask();
            case "upload":
                return new UploadTask();
            default:
                return new EchoTask();
        }
    }
}
