package com.yuditsky.socketapp.multithread.parser;

import com.yuditsky.socketapp.multithread.TransferData;

import java.util.Arrays;

public class TimeParser implements Parser {
    @Override
    public boolean checkCommand(TransferData data) {
        byte[] bytes = data.getValue();
        if (data.getLength() == 4) {
            String str = new String(Arrays.copyOfRange(bytes, 0, (int) data.getLength()));
            return str.equals("time");
        }

        return false;
    }
}
