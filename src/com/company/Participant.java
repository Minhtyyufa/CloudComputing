package com.company;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Participant implements Runnable {
    private AtomicLong balance;
    private AtomicLong newBalance;
    private ReadWriteLock accountLock;
    private Boolean firstCommand = true;
    private String id;
    private Message message;

    public Participant(long balance, String id, Message message) {
        this.balance = new AtomicLong(balance);
        this.id = id;
        this.message = message;
        this.accountLock = new ReentrantReadWriteLock();
    }

    private void handleCommand(String command){
        if(firstCommand){
            newBalance = balance;
            accountLock.writeLock().lock();
            firstCommand = false;
        }
        if(command.split(" ")[1].equals("sub")){
            newBalance.addAndGet(-Long.parseLong(command.split(" ")[2]));
        } else {
            newBalance.addAndGet(Long.parseLong(command.split(" ")[2]));
        }
    }

    // need a two way channel to communicate
    public void run(){
        for(String command = message.getCommand(this.id); !command.split(" ")[1].equals("DONE"); command = message.getCommand(this.id))
        {
            if(command.split(" ")[1].equals("ABORT")){
                //revert changes
                accountLock.writeLock().unlock();
                message.putResponse(this.id + " ACKABORT");
                return;
            } else {
                handleCommand(command);
            }

            System.out.println(command);
        }

        firstCommand = true;

        //For Demonstration Purposes
        System.out.println("Process " + this.id + " has finished");

        //Check if commands lead to a valid balance at the end (ie non-negative)
        if(newBalance.get() < 0){
            message.putResponse(this.id + " NO");
        } else {
            message.putResponse(this.id + " YES");
        }

        if(message.getCommand(this.id).split(" ")[1].equals("COMMIT")) {
            balance = newBalance;
            accountLock.writeLock().unlock();
            message.putResponse(this.id + " ACK");
        }
        else {
            message.putResponse(this.id + " ACKABORT");
        }
    }

}
