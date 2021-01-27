package com.company;

public class Controller implements Runnable {
    private Message message;

    public Controller(Message message){
        this.message = message;
    }

    public void run(){
        String ids[] = {
                "a",
                "b"
        };
        String commands[] = {
                "a add 10",
                "b sub 10",
                "a DONE",
                "b DONE",
        };

        for (String command : commands) {
            message.putCommand(command);
        }

        // Wait for votes from participants
        // Should make this match up with IDs but I'm lazy
        for(int i = 0; i < 2; i++){
            System.out.println(message.getResponse());
        }

        // If all participants voted yes, send out commit messages
        for(String id: ids){
            message.putCommand(id + " COMMIT");
        }

        for(int i = 0; i < 2; i++){
            System.out.println(message.getResponse());
        }

    }
}
