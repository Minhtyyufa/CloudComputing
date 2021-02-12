package main.java.com.multinodetpc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Controller implements Runnable {
    private final MultiNodeMessage message;
    private final static String COMMAND_PATH = "./hard_commands.txt";
    private final String controllerID;

    public Controller(String transactionID) throws IOException {
        this.controllerID = "CONTROLLER" + transactionID;
        this.message = new MultiNodeMessage(this.controllerID);
    }

    // Gets the ids from commands
    private String[] getIDs(List<String> commands){
        HashSet<String> ids = new HashSet<>();
        for(String command : commands){
           ids.add(command.split(" ")[0]);
        }
        return ids.toArray(new String[0]);
    }

    // Reads the commands from a file
    private List<String> readCommands(){
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(COMMAND_PATH));
            List<String> commands = new ArrayList<>();
            for(String line = fileReader.readLine(); line != null; line = fileReader.readLine()){
                commands.add(line);
            }
            return commands;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private void abortTransaction(String[] ids){
        HashSet<String> receivedIdSet = new HashSet<String>();
        HashSet<String> idSet = new HashSet<String>();
        Collections.addAll(idSet, ids);
        for(String id : ids){
            sendToParticipant(id + " ABORT");
        }

        while(!idSet.equals(receivedIdSet)){
            String response = readFromParticipant();
            String[] responseWords = response.split(" ");
            if(responseWords.length <= 2 && responseWords[1].equals("ACKABORT") && !receivedIdSet.contains(responseWords[0])){
                receivedIdSet.add(responseWords[0]);
            }
        }
        System.out.println("Transaction Aborted");

    }

    // Waits for responses from all participants
    // Returns false if one responds with NO
    private boolean receiveFromEachParticipant(String[] ids, String neededResponse){
        HashSet<String> receivedIdSet = new HashSet<String>();
        HashSet<String> idSet = new HashSet<String>();

        Collections.addAll(idSet, ids);
        while(!idSet.equals(receivedIdSet)){
            String response = readFromParticipant();
            String[] responseWords = response.split(" ");
            if(responseWords.length >= 2 && responseWords[1].equals(neededResponse)){
                receivedIdSet.add(responseWords[0]);
            } else if (responseWords.length >= 2 && responseWords[1].equals("NO")){
                return false;
            }
        }
        return true;
    }

    private void sendToParticipant(String message){
        this.message.sendMessage(message + " " + this.controllerID);
    }

    private String readFromParticipant(){
        return this.message.readMessage().split(" ", 2)[1];
    }

    public void run(){
        long startTime = System.nanoTime();
        // All of the commands the participants will do
        List<String> commands = readCommands();

        // All of the participants that the controller will talk to
        String[] ids = getIDs(commands);

        // Send each of the commands to the participants
        for (String command : commands) {
            sendToParticipant(command);
        }
        // Wait for votes from participants
        if(!receiveFromEachParticipant(ids, "YES")){
            // If one voted no, abort the transaction entirely
            abortTransaction(ids);
            long endTime = System.nanoTime();
            System.out.println("Time to complete: " + (endTime - startTime)/1000000 + " milliseconds");
            return;
        }

        // If all participants voted yes, send out commit messages
        for(String id: ids){
            sendToParticipant(id + " COMMIT");
        }

        // Wait for ACKs from each of the participants
        receiveFromEachParticipant(ids, "ACK");

        long endTime = System.nanoTime();
        System.out.println("Time to complete: " + (endTime - startTime)/1000000 + " milliseconds");
    }

}
