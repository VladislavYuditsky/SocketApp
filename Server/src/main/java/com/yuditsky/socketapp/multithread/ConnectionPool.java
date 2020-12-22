package com.yuditsky.socketapp.multithread;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Exchanger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionPool {

    private Map<String, Exchanger<byte[]>> connections = new HashMap<>();

    public boolean exist(String key){
        return connections.containsKey(key);
    }

    public Exchanger<byte[]> replays(String key){
        Exchanger<byte[]> exchanger = new Exchanger<>();
        connections.replace(key, exchanger);
        return exchanger;
    }

    public Exchanger<byte[]> get(String key){
        return connections.get(key);
    }

    public Exchanger<byte[]> create(String key){
        Exchanger<byte[]> exchanger = new Exchanger<>();
        connections.put(key, exchanger);
        return exchanger;
    }

}
