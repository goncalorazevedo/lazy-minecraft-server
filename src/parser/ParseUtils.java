package parser;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ParseUtils {
    public static <E extends Exception> int parseVarint(ByteReader<E> byteReader) throws E {
        int value = 0;
        var position = 0;
        do {
            byte b = byteReader.readByte();
            value |= (b & 0x7F) << position;
            if ((b & 0x80) == 0) {
                return value;
            }
            position += 7;
            if (position >= 35) {
                throw new NumberFormatException("Invalid VarInt");
            }
        } while (true);
    }

    public static <E extends Exception> String parseString(ByteReader<E> byteReader) throws E {
        int length = parseVarint(byteReader);
        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        for (int i = 0; i < length; i++) {
            byteBuffer.put(byteReader.readByte());
        }
        return new String(byteBuffer.array(),  StandardCharsets.UTF_8);
    }

    public static <E extends Exception> short parseUnsignedShort(ByteReader<E> byteReader) throws E {
        return (short) (byteReader.readByte() << 8 | byteReader.readByte());
    }

    public static <E extends Exception> long parseLong(ByteReader<E> byteReader) throws E {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (byteReader.readByte() & 0xFFL);
        }
        return result;
    }

    public static void encodeVarint(int value, Consumer<Byte> byter) {
        while ((value & ~0x7F) != 0) {
            byter.accept((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        byter.accept((byte) value);
    }

    public static void encodeString(String value, Consumer<Byte> byter) {
        encodeVarint(value.length(), byter);
        for (Byte b : value.getBytes(StandardCharsets.UTF_8)) {
            byter.accept(b);
        }
    }

    public static void encodeUnsignedShort(int value, Consumer<Byte> byter) {
        byter.accept((byte) ((value >> 8) & 0xFF));
        byter.accept((byte) (value & 0xFF));
    }

    public static void encodeLong(long value, Consumer<Byte> byter) {
        for (int i = 7; i >= 0; i--) {
            byter.accept((byte) (value >> (i * 8)));
        }
    }
}
