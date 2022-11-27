package org.grpcchat.lib;

import org.grpcchat.internal.ChatGrpc;
import org.grpcchat.internal.Message;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;

import java.util.Date;
import java.util.Scanner;

/**
 * This class represents a chat client that connects to a chat server.
 */
public class ChatClient {
    private final ChatGrpc.ChatStub asyncStub;
    private final String username;
    private final String target;

    public ChatClient(Channel channel, String username, String target) {
        asyncStub = ChatGrpc.newStub(channel);
        this.username = username;
        this.target = target;
    }

    /**
     * Connect to the server and start chat with it.
     */
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

        System.out.println("Client started, connected to " + target);

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
