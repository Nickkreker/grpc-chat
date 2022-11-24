package main;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;

import java.io.IOException;

public class ChatServer {
    private final int port;
    private final String username;
    private final Server server;

    public ChatServer(int port, String username) {
        this.port = port;
        this.username = username;
        ChatService chatService = new ChatService(username);
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(chatService)
                .build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println("Server started, listening on " + port);
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            System.out.println("Terminating server");
        }
    }


}
