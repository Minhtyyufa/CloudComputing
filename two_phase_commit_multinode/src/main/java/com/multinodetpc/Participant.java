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
    private final ReentrantReadWriteLock accountLock = new ReentrantReadWriteLock();
    private final String id;
    private final MultiNodeMessage message;
    private Logger logger = LoggerFactory.getLogger(Participant.class);
    private Hashtable<String, BlockingQueue<String>> transactionMessages = new Hashtable<>();


    // For logging: https://stackoverflow.com/questions/15758685/how-to-write-logs-in-text-file-when-using-java-util-logging-logger
    public Participant(long balance, String id ) throws IOException {
        this.balance = new AtomicLong(balance);
        this.newBalance = new AtomicLong(balance);
        this.id = id;
        this.message = new MultiNodeMessage(id);

//        logger = Logger.getLogger("transaction logger");
//        fh = new FileHandler("./transaction.log");
//        logger.addHandler(fh);
//        SimpleFormatter formatter = new SimpleFormatter();
//        fh.setFormatter(formatter);
    }

    // Thread ends before all of the ACKABORTS can come out
    // Should have a separate thread for each transaction
    // need a two way channel to communicate
    @Override
    public void run(){
        MDC.put("logFileName", this.id);
        while(true){
            String transactionID = null;
            String transactionMessage = null;
            while(transactionID == null){
                transactionMessage = this.message.readMessage();
                transactionID = "CONTROLLER" + transactionMessage.split("CONTROLLER")[1].split(" ")[0];
                if(!transactionID.equals("CONTROLLER1") && !transactionID.equals("CONTROLLER2")){
                    System.out.println("Failed Message: "  + transactionMessage);
                    transactionID = null;
                }
            }

            if(!transactionMessages.containsKey(transactionID)){
                transactionMessages.put(transactionID, new LinkedBlockingQueue<String>());
                (new Thread(new TransactionHandler(transactionID))).start();
            }
            try {
                transactionMessages.get(transactionID).put(transactionMessage);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //MDC.remove("logFileName");
    }

    class TransactionHandler implements Runnable {
        private String transactionID;
        private boolean firstCommand;
        public TransactionHandler(String transactionID){
            this.transactionID = transactionID;
        }
        private void sendToController(String toControllerMessage){
            //logger.info(this.transactionID +" " + toControllerMessage);
            message.sendMessage(this.transactionID +" " + toControllerMessage);
        }
        private String readMessage()  {
            try{
                return transactionMessages.get(this.transactionID).take();
            } catch (Exception e){
                return null;
            }
        }

        private boolean handleCommand(String command){
            if(!accountLock.isWriteLockedByCurrentThread()){
                try{
                    if(!accountLock.writeLock().tryLock(1, TimeUnit.SECONDS)) {
                        logger.info(id + " failed to acquire lock");
                        return false;
                    } else {
                        newBalance = balance;
                    }
                }catch (Exception e){
                    return false;
                }
            }

            if(command.split(" ")[1].equals("sub")){
                newBalance.addAndGet(-Long.parseLong(command.split(" ")[2]));
            } else if(command.split(" ")[1].equals("add")) {
                newBalance.addAndGet(Long.parseLong(command.split(" ")[2]));
            } else {
                logger.info("Unknown command");
            }
            return true;
        }

        @Override
        public void run(){
            System.out.println(this.transactionID);
            for(String command = readMessage(); !command.split(" ")[1].equals("DONE"); command = readMessage())
            {
                //logger.info(command);
                String[] commandWords = command.split(" ");


                if(commandWords[1].equals("ABORT") && commandWords[2].equals(this.transactionID)){
                    //revert changes

                    MDC.remove("logFileName");
                    message.sendMessage(id + " ACKABORT");
                    if(!this.firstCommand)
                        accountLock.writeLock().unlock();
                    this.firstCommand= true;
                    transactionMessages.remove(this.transactionID);
                    return;
                } else {
                    if(!handleCommand(command)){
                        //logger.info(commandWords[3] + " " + id + " NO");
                        logger.info(this.transactionID +" " + id + " triggered Lock Abort");
                        message.sendMessage(this.transactionID + " " + id + " NO");
                        message.sendMessage(this.transactionID + " " + id + " ACKABORT");
                        return;
                    }
                }
            }
            System.out.println("finished commands");
            this.firstCommand = true;
            //logger.info(this.transactionID + " DONE");
            //For Demonstration Purposes
            System.out.println("Process " + id + " has finished");

            //Check if commands lead to a valid balance at the end (ie non-negative)
            if(newBalance.get() < 0){
                sendToController(id + " NO");
            } else {
                sendToController(id + " YES");
            }

            String comMsg = readMessage();
            //Accept messages for COMMIT or ABORT for this transaction. Declines other transactions at this time
            while(!comMsg.split(" ")[2].equals(transactionID)){
                message.sendMessage(comMsg.split(" ")[2] + " " + id + " NO");
                comMsg = message.readMessage();
            }


            if(comMsg.split(" ")[1].equals("COMMIT")) {
                balance = newBalance;
                accountLock.writeLock().unlock();
                //logger.info(id + " ACK");
                sendToController(id + " ACK");
            }
            else {
                accountLock.writeLock().unlock();
                //logger.info(id + " ACKABORT");
                sendToController(id + " ACKABORT");
            }
            transactionMessages.remove(this.transactionID);
        }
    }
}
