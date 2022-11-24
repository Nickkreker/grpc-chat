package main;

import internal.ChatGrpc;
import internal.Message;
import io.grpc.stub.StreamObserver;

import java.util.Date;
import java.util.Scanner;

public class ChatService extends ChatGrpc.ChatImplBase {
    private final String username;

    public ChatService(String username) {
        this.username = username;
    }

    @Override
    public StreamObserver<Message> startChat(StreamObserver<Message> responseObserver) {
        Thread readerThread = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                String msgText = sc.nextLine();
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                Message message = Message.newBuilder()
                        .setMessageText(msgText)
                        .setSenderName(username)
                        .setSendDate(new Date().toString()).build();
                responseObserver.onNext(message);
            }
        });
        readerThread.start();

        final String[] peerName = {null};

        return new StreamObserver<Message>() {
            @Override
            public void onNext(Message value) {
                System.out.printf("%s: %s: %s\n", value.getSendDate(),
                                  value.getSenderName(), value.getMessageText());
                if (peerName[0] == null)
                    peerName[0] = value.getSenderName();
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Peer closed connection");
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                if (peerName[0] == null)
                    System.out.println("Peer closed stream");
                else
                    System.out.printf("%s closed stream\n", peerName[0]);
                readerThread.interrupt();
            }
        };
    }
}
