package com.company;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        Message message = new Message();
        (new Thread(new Controller(message))).start();
        (new Thread(new Participant(0, "a", message))).start();
        (new Thread(new Participant(0, "b", message))).start();
    }
}
