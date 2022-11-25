package main;

import internal.ChatGrpc;
import internal.Message;
import io.grpc.stub.StreamObserver;

import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ChatService extends ChatGrpc.ChatImplBase {
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
     * Close streams with all clients
     */
    public void terminate() {
        for (var connection : connections.values()) {
            connection.onCompleted();
        }
    }
}
