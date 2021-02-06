package main.java.com.company;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Message {
    // Message from controller to participants
    private String command;

    // Response message from participants to controller
    private String response;

    // Indicates that there is no command for the participants
    private boolean commandEmpty = true;

    // Indicates that there is no response for the controller
    private boolean responseEmpty = true;

    // Indicates the transaction is aborted and participants should revert changes
    private boolean isAborted = false;

    private Hashtable<String, BlockingQueue<String>> messageQueues = new Hashtable<>();

    public Message(List<String> ids){
        for(String id: ids){
            messageQueues.put(id.split(" ")[0], new LinkedBlockingQueue<String>());
        }
    }

    public String getCommand(String id){
        String rec_command = null;
        // Returns command to participant when it corresponds to their user
        try{
            while(rec_command == null && !isAborted)
                rec_command = messageQueues.get(id).poll(5, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
        }
        if(isAborted) {
            return id + " ABORT";
        }
        else
            return rec_command;
    }

    public synchronized void putCommand(String command){
        try{
            messageQueues.get(command.split(" ")[0]).put(command);
        } catch (InterruptedException e) {}
    }

    public synchronized String getResponse(){
        while(responseEmpty){
            try {
                wait();
            }
            catch (InterruptedException e) {
            }
        }
        responseEmpty = true;
        notifyAll();
        return response;
    }

    public synchronized boolean putResponse(String response){
        while(!responseEmpty && (!isAborted || response.split(" ")[1].equals("ACKABORT"))){

            try {
                wait();
            } catch (InterruptedException e) {}
        }

        responseEmpty = false;
        this.response = response;
        notifyAll();
        if(isAborted){
            return false;
        }
        return true;
    }

    // Tells all participants to abort the transaction
    public synchronized void sendAbort(){
        isAborted = true;
        notifyAll();
    }

    // Used by the controller once all of the participants have aborted their transactions
    public synchronized void allAborted(){
        isAborted = false;
    }
}
