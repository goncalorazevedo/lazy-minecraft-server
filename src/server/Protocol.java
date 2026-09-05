package server;

import parser.Message;
import parser.MinecraftParser;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Main driver of the client wire protocol (we communicate with the server with this)
 */
public class Protocol {
    private final SocketChannel socketChannel;
    private final MinecraftParser minecraftParser;

    Protocol(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.minecraftParser = new MinecraftParser(socketChannel);
    }

    public void performHandshake(int protoVersion, String addr, int port) throws IOException {
        var handshakeMsg = new Message.Handshake(protoVersion, addr, port, Message.Handshake.Intent.STATUS);
        socketChannel.write(handshakeMsg.toBytes());

        var statusMsg = new Message.StatusRequest();
        socketChannel.write(statusMsg.toBytes());

        var parseResult = minecraftParser.parseMessage();
        if (parseResult.id() != 0x00) {
            throw new IOException("Invalid message, expect status response");
        }
        // discard the response, not used
    }

    public long ping(long timestamp) throws IOException {
        var pingMsg = new Message.PingRequest(timestamp);
        socketChannel.write(pingMsg.toBytes());

        var parseResult = minecraftParser.parseMessage();
        if (parseResult.id() != 0x01) {
            throw new IOException("Invalid message, expect status response");
        }
        var pingResponse = Message.PingResponse.fromBytes(parseResult.payload());
        return pingResponse.timestamp();
    }
}
