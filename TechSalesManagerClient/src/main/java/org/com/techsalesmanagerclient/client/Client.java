package org.com.techsalesmanagerclient.client;


import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.*;
import java.net.Socket;

import static java.lang.System.exit;
import static java.lang.System.in;

public class Client {
    private static PrintWriter outStream;
    private static BufferedReader inStream;
    private static Client instance;

    private Client() {
        connect("localhost", 8080);
    }

    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public static void connect(String ip, int port){
        try {
            Socket clientSocket = new Socket(ip, port);
            outStream = new PrintWriter(clientSocket.getOutputStream(), true);
            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Connected to " + ip + ":" + port);
        }catch (IOException e){
            System.out.println("Can't connect to server" + e.getMessage());
            exit(0);
        }
    }

    public static Response send(Request request)  {
        try {
            outStream.println(JsonUtils.toJson(request));
            return JsonUtils.fromJson(inStream.readLine(), Response.class);

        } catch (IOException e) {
            System.out.println("Can't send request" + e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
