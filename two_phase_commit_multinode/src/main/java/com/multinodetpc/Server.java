package main.java.com.multinodetpc;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;

public class Server {
    static Hashtable<String, ClientHandler> clientHandlerHashtable = new Hashtable<>();

    public static void main(String[] args) throws IOException{
        ServerSocket ss = new ServerSocket(8080);
        Socket s;
        //https://stackoverflow.com/questions/30365250/what-will-happen-if-i-use-socket-setkeepalive-in-node-js-server
        //not the best method though
        s.setKeepAlive(true, 6000);
        // Accepts new clients
        while(true) {
            s = ss.accept();
            System.out.println("New client connected " + s);

            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // First message to server should be id
            String id = dis.readUTF();
            System.out.println("Connected id is " + id);
            ClientHandler clientHandler = new ClientHandler(s,id,dis,dos);

            Thread thread = new Thread(clientHandler);
            clientHandlerHashtable.put(id, clientHandler);
            thread.start();
        }
    }

}
