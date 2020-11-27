package com.yuditsky.socketapp.handler;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
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
        log.debug("Trying to reconnect " + secondsLeft);
        return false;
    }

}
