package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Server {
    private final ServerProcess process;
    private final String addr;
    private final int port;

    public Server(String addr, int port, ServerProcess process) {
        this.addr = addr;
        this.port = port;
        this.process = process;
    }

    public SocketChannel createConnection() throws IOException {
        var sockChannel = SocketChannel.open();
        sockChannel.connect(new InetSocketAddress(addr, port));
        return sockChannel;
    }

    public boolean pingServer(SocketChannel sockChannel) throws IOException {
        Protocol protocol = new Protocol(sockChannel);
        protocol.performHandshake(776, addr, port);
        long currentTime = System.currentTimeMillis();
        return protocol.ping(currentTime) == currentTime;
    }

    public void launchServer() throws IOException {
        System.err.println("Launching server...");
        this.process.launchServer();
    }
}
