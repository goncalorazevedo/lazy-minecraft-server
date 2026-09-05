package parser;

@FunctionalInterface
public interface ByteReader<E extends Exception> {
    byte readByte() throws E;
}
