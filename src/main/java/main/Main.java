package main;

import internal.ChatGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println(args.length);
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
                var client = new ChatClient(channel, username);
                client.sendMessage();
            } finally {
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            }
        }
    }

    public static void printBinary(byte[] out) {
        for (byte b : out) {
            String s = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            System.out.format("%s ", s);
        }
        System.out.println();
    }
}
