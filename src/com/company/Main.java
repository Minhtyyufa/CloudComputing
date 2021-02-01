package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static String PARTICIPANT_INFO_PATH = "./easy_controller_participant_info.txt";

    private static List<String> loadControllerParticipantInfo(){
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(PARTICIPANT_INFO_PATH));
            List<String> participants = new ArrayList<>();
            for(String line = fileReader.readLine(); line != null; line = fileReader.readLine()){
                participants.add(line);
            }
            return participants;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static void main(String[] args) throws IOException {


        Message message = new Message();
        (new Thread(new Controller(message))).start();
        List<String> participants = loadControllerParticipantInfo();
        for(String participant : participants)
        {
            (new Thread(new Participant(Long.parseLong(participant.split(" ")[1]), participant.split(" ")[0], message))).start();
        }


    }
}
