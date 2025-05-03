package org.com.techsalesmanagerserver.server;

import lombok.RequiredArgsConstructor;
import org.com.techsalesmanagerserver.controller.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Component
public class Server implements Runnable {
    private final List<Controller> controllers;
    private final int serverPort;
    private int countClients;
    protected ServerSocket serverSocket;
    protected boolean isStopped = false;

    @Autowired
    public Server(List<Controller> controllers) {
        this.controllers = controllers;
        this.serverPort = 8080;
        this.countClients = 0;
    }

    @Override
    public void run() {

        openServerSocket();

        while (!isStopped) {
            Socket clientSocket = null;
            try {
                clientSocket =  this.serverSocket.accept();
                System.out.println("Accepted connection from Clients" + ++countClients + ": " + this.serverSocket.getLocalSocketAddress());
                ServerHandler handler = new ServerHandler(controllers, clientSocket);
                (new Thread(handler)).start();
            }catch (IOException e) {
                if(isStopped()){
                    System.out.println("Server stopped");
                    return;
                }
                throw new RuntimeException("Can't accept client connection", e);
            }
        }
    }

    private synchronized boolean isStopped(){
        return this.isStopped;
    }

    private synchronized void stop(){
        System.out.println("Server stopped");
        this.isStopped = true;

        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server socket: " + e.getMessage() );
        }
    }

    private void openServerSocket() {
        System.out.println("Open server socket");

        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        }catch (IOException e) {
            throw new RuntimeException("Could not open server socket with port: " + this.serverPort);
        }

    }

}
