package com.yuditsky.socketapp.handler;

import lombok.Data;

@Data
public class ConnectionHandler {

    private final int SECONDS_TO_RECONNECT = 10;
    private int secondsLeft;

    public ConnectionHandler() {
        secondsLeft = SECONDS_TO_RECONNECT;
    }

    public boolean closeConnection() throws InterruptedException {
        Thread.sleep(1000);
        secondsLeft -= 1;
        if (secondsLeft <= 0) {
            return true;
        }
        System.out.println("Trying to reconnect " + secondsLeft);
        return false;
    }

}
