package parser;

import java.nio.ByteBuffer;

public record ParseResult(int id, ByteBuffer payload) {}
