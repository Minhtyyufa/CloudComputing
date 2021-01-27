package com.company;

public class Message {
    // Message from controller to participants
    private String command;

    // Response message from participants to controller
    private String response;

    // Indicates that there is no command for the participants
    private boolean commandEmpty = true;

    // Indicates that there is no response for the controller
    private boolean responseEmpty = true;

    public synchronized String getCommand(String id){
        // Returns command to participant when it corresponds to their user
        while(commandEmpty || !command.split(" ")[0].equals(id)){
            try {
                wait();
            }
            catch (InterruptedException e) {

            }
        }
        commandEmpty = true;
        notifyAll();
        return command;
    }

    public synchronized void putCommand(String command){
        while(!commandEmpty){
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        commandEmpty = false;

        this.command = command;
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

    public synchronized void putResponse(String response){
        while(!responseEmpty){
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        responseEmpty = false;

        this.response = response;
        notifyAll();
    }
}
