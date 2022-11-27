package org.grpcchat.lib;

import org.grpcchat.internal.ChatGrpc;
import org.grpcchat.internal.Message;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a chat server to which chat clients can connect and then exchange messages.
 */
public class ChatServer {
    private final int port;
    private final String username;
    private final Server server;
    private final ChatService service;

    /**
     * ChatServer constructor.
     *
     * @param port port on which server will listen for connections
     * @param username name of a server that will be visible to clients
     */
    public ChatServer(int port, String username) {
        this.port = port;
        this.username = username;
        var chatService = new ChatService(username);
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(chatService)
                .build();
        service = chatService;
    }

    /**
     * Start server and make it wait for client connections.
     *
     * @throws IOException
     */
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

/**
 * This is a util class that encapsulates gRPC logic.
 */
class ChatService extends ChatGrpc.ChatImplBase {
    private final String username;
    private Map<String, StreamObserver<Message>> connections;

    public ChatService(String username) {
        this.username = username;
        connections = new ConcurrentHashMap<>();
    }

    @Override
    public StreamObserver<Message> startChat(StreamObserver<Message> responseObserver) {
        final String[] peerName = {null};

        return new StreamObserver<Message>() {
            @Override
            public void onNext(Message value) {
                if (peerName[0] == null) {
                    peerName[0] = value.getSenderName();
                    connections.put(value.getSenderName(), responseObserver);
                    System.out.printf("%s connected\n", value.getSenderName());
                } else {
                    System.out.printf("%s: %s: %s\n", value.getSendDate(),
                            value.getSenderName(), value.getMessageText());
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Peer closed stream");
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                if (peerName[0] == null)
                    System.out.println("Peer closed stream");
                else
                    System.out.printf("%s closed stream\n", peerName[0]);
            }
        };
    }

    /**
     * Send message to a peer with a given username. If there are multiple peers with the same username,
     * message will be sent only to one of them.
     *
     * @param username username of a peer
     * @param messageText text of a message
     */
    public void sendMessage(String username, String messageText) {
        if (!connections.containsKey(username)) {
            System.out.printf("[ERROR] User %s is not connected\n", username);
            return;
        }

        var observer = connections.get(username);
        try {
            observer.onNext(
                    Message.newBuilder()
                            .setSenderName(this.username)
                            .setMessageText(messageText)
                            .setSendDate(new Date().toString()).build()
            );
        } catch (IllegalStateException e) {
            System.out.println("[ERROR] stream is closed");
        }
    }

    /**
     * Close streams with all clients.
     */
    public void terminate() {
        for (var connection : connections.values()) {
            try {
                connection.onCompleted();
            } catch (IllegalStateException e) {}
        }
    }
}
