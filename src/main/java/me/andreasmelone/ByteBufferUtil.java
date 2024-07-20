package me.andreasmelone;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;

public class ByteBufferUtil {
    public static ByteBuffer concat(final ByteBuffer... buffers) {
        final ByteBuffer combined = ByteBuffer.allocate(Arrays.stream(buffers).mapToInt(ByteBuffer::remaining).sum());
        Arrays.stream(buffers).forEach(b -> combined.put(b.duplicate()));
        combined.flip();
        return combined;
    }

    public static ByteBuffer wrapFile(File file) throws IOException {
        try(InputStream is = Files.newInputStream(file.toPath())) {
            byte[] data = is.readAllBytes();
            return ByteBuffer.wrap(data);
        }
    }

    public static byte[] toByteArray(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    public static void writeToFile(ByteBuffer buffer, File file) throws IOException {
        try(OutputStream os = new FileOutputStream(file)) {
            os.write(toByteArray(buffer));
        }
    }
}
