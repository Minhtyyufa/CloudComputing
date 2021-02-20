package main.java.com.multinodetpc;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private String id;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;



    public ClientHandler(Socket s, String id, DataInputStream dis, DataOutputStream dos){
        this.s = s;
        this.id = id;
        this.dis = dis;
        this.dos = dos;

    }

    public void run(){
        String received;
        while(true) {

            try {
                received = dis.readUTF();
                ClientHandler clientHandler = Server.clientHandlerHashtable.get(received.split(" ")[0]);
                if (clientHandler == null) {
                    dos.writeUTF(received.split(" ")[0] + " NOT FOUND");
                } else {
                    clientHandler.dos.writeUTF(received);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*
        try {
            this.dis.close();
            this.dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
