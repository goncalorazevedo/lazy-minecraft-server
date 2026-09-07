package client;

import parser.Message;
import server.Server;
import server.ServerProcess;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Client {
    private final SocketChannel socketChannel;

    public Client(SocketChannel socketChannel, Server server) {
        this.socketChannel = socketChannel;
    }

    public boolean isLoggingIn() throws IOException {
        var protocol = new Protocol(socketChannel);
        var handshake = protocol.expectHandshake();
        switch (handshake.intent()) {
            case Message.Handshake.Intent.STATUS -> {
                protocol.expectStatus();
                protocol.answerStatus();
                return false;
            }
            case Message.Handshake.Intent.LOGIN -> {
                protocol.answerLogin("{\"text\":\"Server is starting, please reconnect in a bit...\"}");
                return true;
            }
            case Message.Handshake.Intent.TRANSFER -> {
                return false;
            }
        };
        return false;
    }
}
