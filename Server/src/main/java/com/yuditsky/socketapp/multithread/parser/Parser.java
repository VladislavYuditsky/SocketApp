package com.yuditsky.socketapp.multithread.parser;

import com.yuditsky.socketapp.multithread.TransferData;

public interface Parser {

    boolean checkCommand(TransferData data);

}
