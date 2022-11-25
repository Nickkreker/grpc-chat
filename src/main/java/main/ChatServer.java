package main;

import internal.Message;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;

import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

public class ChatServer {
    private final int port;
    private final String username;
    private final Server server;
    private final ChatService service;

    public ChatServer(int port, String username) {
        this.port = port;
        this.username = username;
        ChatService chatService = new ChatService(username);
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(chatService)
                .build();
        service = chatService;
    }

    public void start() throws IOException {
        server.start();
        System.out.println("Server started, listening on " + port);

        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String input = sc.nextLine();
            String[] words = input.split("\\s+->\\s+");
            if (words.length != 2) {
                System.out.println("[ERROR] Wrong input format");
            } else {
                String messageText = words[0];
                String username = words[1];
                service.sendMessage(username, messageText);
            }
        }
        service.terminate();
        server.shutdown();
    }

}
