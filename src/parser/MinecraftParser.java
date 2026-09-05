package parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MinecraftParser {
    private final SocketChannel socketChannel;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(8192);

    public MinecraftParser(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.buffer.limit(0);
    }

    private void fillBuffer() throws IOException {
        buffer.clear();

        int read = socketChannel.read(buffer);
        if (read == -1) {
            throw new IOException("Connection closed by client while reading");
        }

        buffer.flip(); // Prepare buffer for application read
    }

    private byte readByte() throws IOException {
        if (!buffer.hasRemaining()) {
            fillBuffer();
        }
        return buffer.get();
    }

    public ByteBuffer readBytes(int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        for (int i = 0; i < length; i++) {
            buffer.put(readByte());
        }
        buffer.flip();
        return buffer;
    }

    public ParseResult parseMessage() throws IOException {
        int length = ParseUtils.parseVarint(this::readByte);
        ByteBuffer payload = readBytes(length);
        int id = ParseUtils.parseVarint(payload::get);
        return new ParseResult(id, payload);
    }
}
