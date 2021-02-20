package main.java.com.multinodetpc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Controller implements Runnable {
    private final MultiNodeMessage message;
    private final static String COMMAND_PATH = config_file.json;
    private final String controllerID;
    private HashSet<String> abortedIds = new HashSet<>();

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

    private static void parseCommandObject(JSONObject transaction) {
        //Get transaction object within list
        JSONObject transactionObject = (JSONObject) transaction.get("transaction");

        //Get Command
        String command = (String) transactionObject.get("command");

    }

    // Reads the commands from a file
    private List<String> readCommands(){
        JSONParser jsonParser = new JSONParser();
        try () {
            //Read JSON files
            JSONArray transactionsArray = (JSONArray) jsonParser.parse(new FileReader(config_path.json));
            List<String> commands = new ArrayList<>();

            for (Object transactionObject: transactionsArray) {
                JSONObject transaction = (JSONObject) transactionObject;
                String command = (String) transaction.get("command");
                commands.add(command);
            }

            return commands;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private void abortTransaction(String[] ids){

        HashSet<String> idSet = new HashSet<String>();
        Collections.addAll(idSet, ids);
        for(String id : ids){
            sendToParticipant(id + " ABORT");
        }

        while(!idSet.equals(abortedIds)){
            String response = readFromParticipant();
            String[] responseWords = response.split(" ");
            /*
            if(responseWords.length <= 2 && responseWords[1].equals("ACKABORT") && !abortedIds.contains(responseWords[0])){
                abortedIds.add(responseWords[0]);
            }
             */
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
        String msg = this.message.readMessage().split(" ", 2)[1];
        if(msg.split(" ")[1].equals("ACKABORT")){
            abortedIds.add(msg.split(" ")[0]);
        }
        return msg;
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
//            sendToParticipant(id + "ALIVE_CONTROLLER");
        }
//        try {
//            while(receiveFromEachParticipant(ids, "ALIVE")) {
//                wait(TimeInterval);
//                System.out.println("Controller: " + "I'm alive");
//                sendToParticipant(id + "ALIVE_CONTROLLER");
//                // Let the thread sleep for a while.
//                Thread.sleep(TimeInterval * 1000);
//            }
//        } catch (InterruptedException e) {
//            System.out.println("Controller interrupted.");
//        }

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
