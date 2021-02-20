package main.java.com.multinodetpc;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.FileHandler;

public class Participant implements Runnable {

    //idk man just trying shit out
    //https://stackoverflow.com/questions/33869092/java-heartbeat-design
    //https://docs.oracle.com/cd/E19206-01/816-4178/6madjde6e/index.html

    private int TimeInterval = 5; //seconds
    private String HeartBeat = "HeartbeatAgent";
    private HashMap<Integer, Object> values; // <id, value>

    private AtomicLong balance;
    private AtomicLong newBalance;
    private final ReadWriteLock accountLock = new ReentrantReadWriteLock();
    private Boolean firstCommand = true;
    private final String id;
    private final MultiNodeMessage message;
    private static Logger logger = LoggerFactory.getLogger(Participant.class);
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
        this.message.sendMessage(this.transactionID +" " + message);
    }
//    public void connectionEstablished (String message) {}
//    public void connectionRetrying () {}
//    public void connectionReestablished () {}
//    public void connectionLost () {}
//    public void connectionEliminated () {}
    private void handleCommand(String command){
        if(firstCommand){
            accountLock.writeLock().lock();
            newBalance = balance;
            transactionID = command.split(" ")[3];
            firstCommand = false;
        }
        if(command.split(" ")[1].equals("sub")){
            newBalance.addAndGet(-Long.parseLong(command.split(" ")[2]));
        } else {
            newBalance.addAndGet(Long.parseLong(command.split(" ")[2]));
        }
    }

    // need a two way channel to communicate
    @Override
    public void run(){
        MDC.put("logFileName", id);

        System.out.println("Running " +  this.id );
//        try {
//            while(command.split(" ")[1].equals("ALIVE_CONTROLLER")) {
//                wait(TimeInterval);
//                System.out.println("Thread: " + this.id + ", " + "I'm alive");
//                sendToController(this.id + " ALIVE")
//                // Let the thread sleep for a while.
//                Thread.sleep(TimeInterval * 1000);
//            }
//        } catch (InterruptedException e) {
//            System.out.println("Thread " +  this.id + " interrupted.");
//        }
        //System.out.println("Thread " +  this.id + " exiting.");


        for(String command = message.readMessage(); !command.split(" ")[1].equals("DONE"); command = message.readMessage())
        {
            logger.info(command);

            // abort other incoming transactions if currently in one transaction
            if(!firstCommand && !command.split(" ")[3].equals(this.transactionID)){
                logger.info(command.split(" ")[3] + " " + id + " NO");
                message.sendMessage(command.split(" ")[3] + " " + id + " NO");
                message.sendMessage(command.split(" ")[3] + " " + id + " ACKABORT");
            }


            if(command.split(" ")[1].equals("ABORT")){
                //revert changes
                if(!firstCommand)
                    accountLock.writeLock().unlock();
                MDC.remove("logFileName");
                message.sendMessage(this.id + " ACKABORT");
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

        String comMsg = message.readMessage();
        //Accept messages for COMMIT or ABORT for this transaction. Declines other transactions at this time
        while(!comMsg.split(" ")[2].equals(transactionID)){
            message.sendMessage(comMsg.split(" ")[2] + " " + id + " NO");
            comMsg = message.readMessage();
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
