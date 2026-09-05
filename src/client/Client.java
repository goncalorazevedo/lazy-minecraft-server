package client;

import parser.Message;
import server.Server;
import server.ServerProcess;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Client {
    private final SocketChannel socketChannel;
    private final Server server;

    public Client(SocketChannel socketChannel, Server server) {
        this.socketChannel = socketChannel;
        this.server = server;
    }

    public void proccessClient() throws IOException {
        var protocol = new Protocol(socketChannel);
        var handshake = protocol.expectHandshake();
        switch (handshake.intent()) {
            case Message.Handshake.Intent.STATUS -> {
                protocol.expectStatus();
                protocol.answerStatus();
            }
            case Message.Handshake.Intent.LOGIN -> {
                server.launchServer();
                protocol.answerLogin("{\"text\":\"Server is starting, please reconnect in a bit...\"}");
            }
            case Message.Handshake.Intent.TRANSFER -> {
                return;
            }
        };
    }
}
