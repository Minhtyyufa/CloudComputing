package com.company;

public class Controller implements Runnable {
    private Message message;

    public Controller(Message message){
        this.message = message;
    }

    public void run(){
        String commands[] = {
                "a add 10",
                "b sub 10"
        };

        for(int i = 0; i < commands.length; i ++){
            message.putCommand(commands[i]);

        }
    }
}
