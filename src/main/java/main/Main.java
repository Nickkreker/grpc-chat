package main;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length == 2) {
            String username = args[0];
            String port = args[1];
            ChatServer server = new ChatServer(Integer.parseInt(port), username);
            server.start();
        } else {
            String username = args[0];
            String ip = args[1];
            String port = args[2];

            String target = ip + ":" + port;
            ManagedChannel channel =
                    ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            try {
                var client = new ChatClient(channel, username, target);
                client.startChat();
            } finally {
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            }
        }
    }
}
