package com.company;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Participant implements Runnable {
    private AtomicLong balance;
    private AtomicLong newBalance;
    private final ReadWriteLock accountLock = new ReentrantReadWriteLock();
    private Boolean firstCommand = true;
    private final String id;
    private final Message message;
    private static Logger logger;
    private FileHandler fh;

    // For logging: https://stackoverflow.com/questions/15758685/how-to-write-logs-in-text-file-when-using-java-util-logging-logger
    public Participant(long balance, String id, Message message) throws IOException {
        this.balance = new AtomicLong(balance);
        this.id = id;
        this.message = message;
        logger = Logger.getLogger(id);
        fh = new FileHandler("./" + id + ".log");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
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
            logger.info(command);
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
