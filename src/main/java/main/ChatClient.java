package main;

import internal.ChatGrpc;
import internal.Message;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;

import java.util.Date;
import java.util.Scanner;

public class ChatClient {
    private final ChatGrpc.ChatBlockingStub blockingStub;
    private final ChatGrpc.ChatStub asyncStub;
    private final String username;

    public ChatClient(Channel channel, String username) {
        blockingStub = ChatGrpc.newBlockingStub(channel);
        asyncStub = ChatGrpc.newStub(channel);
        this.username = username;
    }

    public void startChat() {
        StreamObserver<Message> requestObserver =
                asyncStub.startChat(new StreamObserver<Message>() {
                    @Override
                    public void onNext(Message value) {
                        System.out.printf("%s: %s: %s\n", value.getSendDate(),
                                          value.getSenderName(), value.getMessageText());
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (t instanceof IllegalStateException) {
                            System.out.println("Peer closed connection");
                        }
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Server closed connection");
                    }
                });

        // Send greeting message with a username
        requestObserver.onNext(Message.newBuilder().setSenderName(username).build());

        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String msgText = sc.nextLine();
            Message message = Message.newBuilder()
                    .setMessageText(msgText)
                    .setSenderName(username)
                    .setSendDate(new Date().toString()).build();
            requestObserver.onNext(message);
        }

        requestObserver.onCompleted();
    }
}
