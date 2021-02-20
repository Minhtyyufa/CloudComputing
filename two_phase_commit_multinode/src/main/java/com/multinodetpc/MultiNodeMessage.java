package main.java.com.multinodetpc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MultiNodeMessage {
    private DataInputStream dis;
    private DataOutputStream dos;

    //Change these to configure server settings
    private final int serverPort = 8080;
    private String IP_ADDRESS;

    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();


    public MultiNodeMessage(String id) throws IOException {
        try {
            JSONReader jsonReader = new JSONReader();
            IP_ADDRESS = jsonReader.getAttribute("server_ip");
        } catch (Exception e){
            IP_ADDRESS = "localhost";
        }
        InetAddress ip = InetAddress.getByName(IP_ADDRESS);
        Socket s = new Socket(ip, serverPort);
        dis = new DataInputStream(s.getInputStream());
        dos = new DataOutputStream(s.getOutputStream());
        dos.writeUTF(id);
        Thread getMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        String msg = dis.readUTF();
                        messageQueue.add(msg);
                    } catch (IOException e){
                        //e.printStackTrace();
                    }
                }
            }
        });
        getMessage.start();
    }

    public void sendMessage(String message) {
        try{

            dos.writeUTF(message);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public String readMessage(){
        try {
            String recMessage = null;
            while(recMessage == null){
                recMessage = messageQueue.poll(5, TimeUnit.MILLISECONDS);
            }

            return recMessage;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
