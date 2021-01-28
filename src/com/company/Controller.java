package com.company;

import java.util.Collections;
import java.util.HashSet;

public class Controller implements Runnable {
    private Message message;

    public Controller(Message message){
        this.message = message;
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
    public void run(){
        String[] ids = {
                "a",
                "b"
        };
        String[] commands = {
                "a add 10",
                "b sub 10",
                "a DONE",
                "b DONE",
        };

        for (String command : commands) {
            message.putCommand(command);
        }


        abortTransaction(ids);

        // Wait for votes from participants
        // Should make this match up with IDs but I'm lazy
        for(int i = 0; i < 2; i++){
            System.out.println(message.getResponse());
        }

//
//        // If all participants voted yes, send out commit messages
//        for(String id: ids){
//            message.putCommand(id + " COMMIT");
//        }
//
//        for(int i = 0; i < 2; i++){
//            System.out.println(message.getResponse());
//        }
    }
}
