package main.java.com.multinodetpc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static List<String> loadControllerParticipantInfo(){
        try {
            String PARTICIPANT_INFO_PATH;
            try {
                JSONReader jsonReader = new JSONReader();
                PARTICIPANT_INFO_PATH = jsonReader.getAttribute("participant_info");
            } catch (Exception e){
                PARTICIPANT_INFO_PATH = "easy_controller_participant_info.txt";
            }
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
    public static void main(String[] args) throws IOException, InterruptedException {
        Main m = new Main();
        List<String> participants = loadControllerParticipantInfo();
        Thread controllerThread1 = new Thread(new Controller("1"));
        Thread controllerThread2 = new Thread(new Controller("2"));
        for(String participant : participants)
        {
            (new Thread(new Participant(Long.parseLong(participant.split(" ")[1]), participant.split(" ")[0]))).start();
        };
        synchronized(m){
            m.wait(1000);
            controllerThread1.start();
            controllerThread2.start();
        }

    }
}
