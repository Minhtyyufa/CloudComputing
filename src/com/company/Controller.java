package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Controller implements Runnable {
    private final Message message;
    private final static String COMMAND_PATH = "./easy_abort_commands.txt";
    public Controller(Message message){
        this.message = message;
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

        message.sendAbort();
        System.out.println("idSet: " + idSet);
        while(!idSet.equals(receivedIdSet)){
            String response = message.getResponse();
            String[] responseWords = response.split(" ");
            System.out.println(response);
            if(responseWords.length >= 2 && responseWords[1].equals("ACKABORT") && !receivedIdSet.contains(responseWords[0])){
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
            String response = message.getResponse();
            String[] responseWords = response.split(" ");
            System.out.println(response);
            if(responseWords.length >= 2 && responseWords[1].equals(neededResponse)){
                receivedIdSet.add(responseWords[0]);
            } else if (responseWords.length >= 2 && responseWords[1].equals("NO")){
                return false;
            }
        }
        return true;
    }
    public void run(){
        long startTime = System.nanoTime();
        // All of the commands the participants will do
        List<String> commands = readCommands();

        // All of the participants that the controller will talk to
        String[] ids = getIDs(commands);

        // Send each of the commands to the participants
        for (String command : commands) {
            message.putCommand(command);
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
            message.putCommand(id + " COMMIT");
        }

        // Wait for ACKs from each of the participants
        receiveFromEachParticipant(ids, "ACK");

        long endTime = System.nanoTime();
        System.out.println("Time to complete: " + (endTime - startTime)/1000000 + " milliseconds");
    }
}
