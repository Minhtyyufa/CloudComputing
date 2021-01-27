package com.company;

import java.util.concurrent.atomic.AtomicLong;

public class Participant extends Runnable{
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
        for(String command = message.getCommand(this.id); !command.split(" ")[1].equals("COMMIT") && !command.split(" ")[1].equals("ABORT"); command = message.getCommand(this.id))
        {

        }
    }




}
