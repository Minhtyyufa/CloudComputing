package main.java.com.company;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
public class Message {
    // trying to create hashtable
    Hashtable<String, BlockingQueue> commands = new Hashtable<String, BlockingQueue>();

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


    public synchronized String getCommand(String id){
        // Returns command to participant when it corresponds to their user
        while(commandEmpty || !command.split(" ")[0].equals(id)){
            try {
                wait();
                if(isAborted)
                    break;
            }
            catch (InterruptedException e) {

            }
        }
        commandEmpty = true;
        notifyAll();
        if(isAborted) {
            return id + " ABORT";
        }
        else
            return hashtable(id).get();
            //return command;
    }

    public synchronized void putCommand(String command){
        while(!commandEmpty){
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        commandEmpty = false;

        commands.put(command);
        //this.command = command;
        notifyAll();
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
