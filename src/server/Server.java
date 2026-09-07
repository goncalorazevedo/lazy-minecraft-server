package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private final String addr;
    private final int port;
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    private final ServerProcessActor serverProcessActor;

    public enum State { STOPPED, RUNNING }

    public Server(String addr, int port, ServerProcess serverProcess) {
        this.addr = addr;
        this.port = port;
        this.serverProcessActor = new ServerProcessActor(serverProcess, this);
    }

    public void incrementConnections() { activeConnections.incrementAndGet(); }
    public void decrementConnections() { activeConnections.decrementAndGet(); }
    public int getConnections() { return activeConnections.get(); }

    public SocketChannel createConnection() throws IOException {
        var sockChannel = SocketChannel.open();
        sockChannel.connect(new InetSocketAddress(addr, port));
        return sockChannel;
    }

    private boolean pingServer(SocketChannel sockChannel) throws IOException {
        Protocol protocol = new Protocol(sockChannel);
        protocol.performHandshake(776, addr, port);
        long currentTime = System.currentTimeMillis();
        return protocol.ping(currentTime) == currentTime;
    }

    public boolean isUp() {
        try (SocketChannel pingChannel = createConnection()) {
            return pingServer(pingChannel);
        } catch (IOException e) {
            return false;
        }
    }

    public Server.State getServerState() {
        return serverProcessActor.getState();
    }

    public void launch() { serverProcessActor.submit(serverProcessActor::launch); }
    public void stop() { serverProcessActor.submit(serverProcessActor::stop); }
}
