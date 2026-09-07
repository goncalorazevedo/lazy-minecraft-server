package proxy;

import client.Client;
import server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.StructuredTaskScope;

public class ProxyServer {
	private final int listenPort;
	private final Server server;

	public ProxyServer(int listenPort, Server server) {
		this.listenPort = listenPort;
		this.server = server;
	}

	public void run() {
		Thread mainThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			mainThread.interrupt();
			try { mainThread.join(); } catch (InterruptedException ignored) {
			}
		}));

		try (var scope = StructuredTaskScope.open(
				StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
			listener(scope);
			scope.join();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			System.out.println("Shutting down...");
		}
	}

	public void listener(StructuredTaskScope<Object, ?> parentScope) throws IOException {
		try (var srvSockChannel = ServerSocketChannel.open()) {
			srvSockChannel.socket().setReuseAddress(true);
			srvSockChannel.socket().bind(new InetSocketAddress("0.0.0.0", listenPort));
            do {
                var clientSockChannel = srvSockChannel.accept();
                parentScope.fork(() -> {
                    handleClient(clientSockChannel);
                    return null;
                });
            } while (true);
		}
	}

	public void handleClient(SocketChannel clientSockChannel) {
		try (clientSockChannel) {
			if (server.getServerState() == Server.State.STOPPED) {
				var client = new Client(clientSockChannel, server);
				if (client.isLoggingIn()) {
					server.launch();
				}
			} else {
				try (var srvSockChannel = server.createConnection()) {
					proxyBidirectional(clientSockChannel, srvSockChannel);
				}
			}
        } catch (Throwable t) {
			System.err.println("Error handling client: " + t);
		} finally {
			System.out.println("Finished handling client");
		}
	}

	public void proxyBidirectional(SocketChannel clientSockChannel, SocketChannel srvSockChannel) {
		try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
			server.incrementConnections();
			scope.fork(() -> { proxyConnections(clientSockChannel, srvSockChannel); return null; });
			scope.fork(() -> { proxyConnections(srvSockChannel, clientSockChannel); return null; });
			scope.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			server.decrementConnections();
        }
	}

	public void proxyConnections(SocketChannel src, SocketChannel dst) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
		do {
			int toCopy = src.read(buffer);
			if (toCopy == -1) {
				dst.socket().shutdownOutput();
				break;
			}
			buffer.flip();
			while (toCopy > 0) {
				int written = dst.write(buffer);
				toCopy -= written;
			}
			buffer.clear();
		} while (true);
	}
}

