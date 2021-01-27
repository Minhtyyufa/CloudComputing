package com.company;

public class Message {
    private String command;

    private boolean empty = true;

    public synchronized String getCommand(String id){
        // Returns command to participant when it corresponds to their user
        while(empty || !command.split(" ")[0].equals(id)){
            try {
                wait();
            }
            catch (InterruptedException e) {

            }
        }

        empty = true;
        notifyAll();
        return command;
    }

    public synchronized void putCommand(String command){
        while(!empty){
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        empty = false;

        this.command = command;
        notifyAll();
    }

}
