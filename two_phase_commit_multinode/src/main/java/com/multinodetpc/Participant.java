package main.java.com.multinodetpc;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Hashtable;

public class Participant implements Runnable {

    private AtomicLong balance;
    private AtomicLong newBalance;
    private final ReadWriteLock accountLock = new ReentrantReadWriteLock();
    private Boolean firstCommand = true;
    private final String id;
    private final MultiNodeMessage message;
    private Logger logger = LoggerFactory.getLogger(Participant.class);
    private String transactionID;



    // For logging: https://stackoverflow.com/questions/15758685/how-to-write-logs-in-text-file-when-using-java-util-logging-logger
    public Participant(long balance, String id ) throws IOException {
        this.balance = new AtomicLong(balance);
        this.id = id;
        this.message = new MultiNodeMessage(id);

//        logger = Logger.getLogger("transaction logger");
//        fh = new FileHandler("./transaction.log");
//        logger.addHandler(fh);
//        SimpleFormatter formatter = new SimpleFormatter();
//        fh.setFormatter(formatter);
    }

    private void sendToController(String message){
        this.logger.info(this.transactionID +" " + message);
        this.message.sendMessage(this.transactionID +" " + message);
    }

    private boolean handleCommand(String command){
        if(firstCommand){
            try{
                if(!accountLock.writeLock().tryLock(1, TimeUnit.SECONDS)) {
                    logger.info(this.id + " failed to acquire lock");
                    return false;
                }
                newBalance = balance;
                transactionID = command.split(" ")[3];
                firstCommand = false;
                return true;
            } catch (Exception e){
                return false;
            }
        }
        if(command.split(" ")[1].equals("sub")){
            newBalance.addAndGet(-Long.parseLong(command.split(" ")[2]));
        } else if(command.split(" ")[1].equals("add")) {
            newBalance.addAndGet(Long.parseLong(command.split(" ")[2]));
        } else {
            this.logger.info("Unknown command");
        }
        return true;
    }

    // Thread ends before all of the ACKABORTS can come out
    // Should have a separate thread for each transaction
    // need a two way channel to communicate
    @Override
    public void run(){
        MDC.put("logFileName", this.id);
        for(String command = this.message.readMessage(); !command.split(" ")[1].equals("DONE"); command = this.message.readMessage())
        {
            //logger.info(command);
            String[] commandWords = command.split(" ");
            // abort other incoming transactions if currently in one transaction
            if(!firstCommand && commandWords.length > 3 &&  !commandWords[3].equals(this.transactionID)){
                //logger.info(commandWords[3] + " " + this.id + " NO");
                this.message.sendMessage(commandWords[3] + " " + this.id + " NO");
                this.message.sendMessage(commandWords[3] + " " + this.id + " ACKABORT");
                continue;
            }


            if(commandWords[1].equals("ABORT") && commandWords[2].equals(this.transactionID)){
                //revert changes
                if(!firstCommand)
                    accountLock.writeLock().unlock();
                MDC.remove("logFileName");
                this.message.sendMessage(this.id + " ACKABORT");
                firstCommand= true;
                return;
            } else {
                handleCommand(command);
            }
        }
        System.out.println("finished commands");
        firstCommand = true;
        logger.info(this.id + " DONE");
        //For Demonstration Purposes
        System.out.println("Process " + this.id + " has finished");

        //Check if commands lead to a valid balance at the end (ie non-negative)
        if(newBalance.get() < 0){
            logger.info(this.id + " NO");
            sendToController(this.id + " NO");
        } else {
            logger.info(this.id + " YES");
            sendToController(this.id + " YES");
        }

        String comMsg = this.message.readMessage();
        //Accept messages for COMMIT or ABORT for this transaction. Declines other transactions at this time
        while(!comMsg.split(" ")[2].equals(transactionID)){
            this.message.sendMessage(comMsg.split(" ")[2] + " " + id + " NO");
            comMsg = this.message.readMessage();
        }


        if(comMsg.split(" ")[1].equals("COMMIT")) {
            balance = newBalance;
            accountLock.writeLock().unlock();
            logger.info(this.id + " ACK");
            sendToController(this.id + " ACK");
        }
        else {
            accountLock.writeLock().unlock();
            logger.info(this.id + " ACKABORT");
            sendToController(this.id + " ACKABORT");
        }
        MDC.remove("logFileName");
    }
}
