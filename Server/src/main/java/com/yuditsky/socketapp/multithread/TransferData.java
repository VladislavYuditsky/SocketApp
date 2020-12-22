package com.yuditsky.socketapp.multithread;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferData {
    private InetAddress address;
    private int port;
    private byte[] value;
    private long length;

    public String getKey(){
        return address.toString() + port;
    }
}
