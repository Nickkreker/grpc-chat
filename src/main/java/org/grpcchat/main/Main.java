package org.grpcchat.main;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.grpcchat.lib.ChatClient;
import org.grpcchat.lib.ChatServer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * This class is an entry point to the program.
 */
public class Main {
    /**
     * Launches a server or a client based on passed arguments.
     */
    public static void main(String[] args) throws InterruptedException, IOException {

        if (args.length == 2) {
            String username = args[0];
            String port = args[1];
            ChatServer server = new ChatServer(Integer.parseInt(port), username);
            server.start();
        } else if (args.length == 3) {
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
        } else {
            System.out.println("[ERROR] Wrong number of args. Specify name and port to run as a server or " +
                    "username, ip and port of a server to run as a client.");
        }
    }
}
