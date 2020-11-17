package com.yuditsky.socketapp.entity;

import lombok.Data;

@Data
public abstract class Client {
    private boolean connected = false;
}
