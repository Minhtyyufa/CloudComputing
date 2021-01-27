package com.company;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Message message = new Message();
        (new Thread(new Controller(message))).start();
        (new Thread(new Participant(0, "a", message))).start();
        (new Thread(new Participant(0, "b", message))).start();
    }
}
