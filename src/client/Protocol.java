package client;

import parser.Message;
import parser.MinecraftParser;

import java.io.IOException;
import java.nio.channels.SocketChannel;


/**
 * Main driver of the server wire protocol (clients communicate via this class)
 */
public class Protocol {
    private final SocketChannel socketChannel;
    private final MinecraftParser minecraftParser;

    public Protocol(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.minecraftParser = new MinecraftParser(socketChannel);
    }

    public Message.Handshake expectHandshake() throws IOException {
        var parseResult = minecraftParser.parseMessage();
        if (parseResult.id() != 0x00) {
            throw new IOException("Invalid handshake sent by client");
        }
        return Message.Handshake.fromBytes(parseResult.payload());
    }

    public void expectStatus() throws IOException {
        var parseResult = minecraftParser.parseMessage();
        if (parseResult.id() != 0x00) {
            throw new IOException("Invalid handshake sent by client");
        }
        // ignore payload
    }

    public void answerStatus() throws IOException {
        String json = "{\"version\":{\"name\":\"1.26.2\",\"protocol\":776},\"players\":{\"max\":20,\"online\":0},\"description\":{\"text\":\"Server is idle.\"}}";
        var statusResponse = new Message.StatusResponse(json);
        socketChannel.write(statusResponse.toBytes());
    }

    public void answerLogin(String reason) throws IOException {
        var loginDisconnect = new Message.LoginDisconnect(reason);
        socketChannel.write(loginDisconnect.toBytes());
    }
}
