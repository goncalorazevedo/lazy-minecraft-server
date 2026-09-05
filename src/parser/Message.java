package parser;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public sealed interface Message permits
        Message.Handshake,
        Message.StatusRequest,
        Message.StatusResponse,
        Message.PingRequest,
        Message.PingResponse,
        Message.LoginDisconnect,
        Message.Unsupported
{
    /** Frames the message with varint length prefix */
    static ByteBuffer frameToBytes(ByteBuffer payload) {
        int payloadSize = payload.position();
        payload.flip();
        ByteBuffer message = ByteBuffer.allocate(payloadSize + 64);
        ParseUtils.encodeVarint(payloadSize, message::put);
        message.put(payload);
        message.flip();
        return message;
    }

    record Handshake(
            int protocolVersion,
            String serverAddress,
            int serverPort,
            Intent intent
    ) implements Message {

        public static Handshake fromBytes(ByteBuffer payload) {
            int protoVersion = ParseUtils.parseVarint(payload::get);
            String serverAddr =  ParseUtils.parseString(payload::get);
            int serverPort = ParseUtils.parseUnsignedShort(payload::get);
            Intent intent = Intent.fromInt(ParseUtils.parseVarint(payload::get));
            return new Handshake(protoVersion, serverAddr, serverPort, intent);
        }

        public ByteBuffer toBytes() {
            ByteBuffer payload = ByteBuffer.allocate(1024);
            ParseUtils.encodeVarint(0x00, payload::put); // message ID
            ParseUtils.encodeVarint(protocolVersion, payload::put);
            ParseUtils.encodeString(serverAddress, payload::put);
            ParseUtils.encodeUnsignedShort(serverPort, payload::put);
            ParseUtils.encodeVarint(intent.wireValue, payload::put);
            return frameToBytes(payload);
        }

        public enum Intent {
            STATUS(1), LOGIN(2), TRANSFER(3);

            protected final int wireValue;

            Intent(int wireValue) { this.wireValue = wireValue; }

            public static Intent fromInt(int value) {
                return switch (value) {
                    case 1 -> STATUS;
                    case 2 -> LOGIN;
                    case 3 -> TRANSFER;
                    default -> throw new IllegalArgumentException("Unknown: " + value);
                };
            }
        }
    }

    record StatusRequest() implements Message {

        public ByteBuffer toBytes() {
            ByteBuffer payload = ByteBuffer.allocate(32);
            ParseUtils.encodeVarint(0x00, payload::put); // message ID
            return frameToBytes(payload);
        }

        public static StatusRequest fromBytes(ByteBuffer payload) {
            return new StatusRequest();
        }
    }

    record StatusResponse(
            String status
    ) implements Message {

        public static StatusResponse fromBytes(ByteBuffer payload) {
            return new StatusResponse(ParseUtils.parseString(payload::get));
        }

        public ByteBuffer toBytes() {
            ByteBuffer payload = ByteBuffer.allocate(status.getBytes(StandardCharsets.UTF_8).length + 64);
            ParseUtils.encodeVarint(0x00, payload::put); // message ID
            ParseUtils.encodeString(status, payload::put);
            return frameToBytes(payload);
        }
    }
    record PingRequest(
            long timestamp
    ) implements Message {

        public static PingRequest fromBytes(ByteBuffer payload) {
            long timestamp = ParseUtils.parseLong(payload::get);
            return new PingRequest(timestamp);
        }

        public ByteBuffer toBytes() {
            ByteBuffer payload = ByteBuffer.allocate(64);
            ParseUtils.encodeVarint(0x01, payload::put);
            ParseUtils.encodeLong(timestamp, payload::put);
            return frameToBytes(payload);
        }
    }

    record PingResponse(
            long timestamp
    ) implements Message {

        public static PingResponse fromBytes(ByteBuffer payload) {
            return new PingResponse(ParseUtils.parseLong(payload::get));
        }

        public ByteBuffer toBytes() {
            ByteBuffer payload = ByteBuffer.allocate(64);
            ParseUtils.encodeLong(timestamp, payload::put);
            return frameToBytes(payload);
        }
    }

    record LoginDisconnect(
            String reason
    ) implements Message {
        public ByteBuffer toBytes() {
            ByteBuffer payload = ByteBuffer.allocate(reason.length() + 64);
            ParseUtils.encodeVarint(0x00, payload::put); // message ID
            ParseUtils.encodeString(reason, payload::put);
            return frameToBytes(payload);
        }
    }

    record Unsupported() implements Message {}
}
