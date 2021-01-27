package com.company;

import java.util.concurrent.atomic.AtomicLong;

public class Participant implements Runnable {
    private AtomicLong balance;
    private String id;
    private Message message;

    public Participant(long balance, String id, Message message) {
        this.balance = new AtomicLong(balance);
        this.id = id;
        this.message = message;
    }



    // need a two way channel to communicate
    public void run(){
        for(String command = message.getCommand(this.id); !command.split(" ")[1].equals("DONE"); command = message.getCommand(this.id))
        {
            if(command.split(" ")[1].equals("ABORT")){
                //revert changes
                return;
            }
            System.out.println(command);
        }

        //For Demonstration Purposes
        System.out.println("Process " + this.id + " has finished");

        message.putResponse(this.id + " YES");

        if(message.getCommand(this.id).split(" ")[1].equals("COMMIT"))
            message.putResponse(this.id + " ACK");
        else
            message.putResponse(this.id + " ABORT");
    }

}
