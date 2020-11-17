package com.yuditsky.socketapp.view;

import com.yuditsky.socketapp.controller.ClientController;

import java.util.Scanner;

public class View implements Runnable{
    private ClientController clientController;

    public View(ClientController clientController){
        this.clientController = clientController;
        run();
    }

    @Override
    public void run(){
        Scanner in = new Scanner(System.in);
        String request;

        while(!(request = in.nextLine()).equals("exit")){
            System.out.println(clientController.executeTask(request));
        }
    }
}
