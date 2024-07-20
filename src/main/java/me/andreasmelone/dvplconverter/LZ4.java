package me.andreasmelone.dvplconverter;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.nio.ByteBuffer;

public class LZ4 {
    private final static LZ4Factory FACTORY = LZ4Factory.fastestInstance();

    /**
     * Encodes a buffer using LZ4HC
     *
     * @param target The buffer into which the result will be written
     * @param source The uncompressed buffer
     * @return The size of the compressed buffer
     */
    public static int encodeBlockHC(ByteBuffer target, ByteBuffer source) {
        LZ4Compressor compressor = FACTORY.highCompressor();
        return compressor.compress(
                source,
                source.capacity() - source.remaining(),
                source.capacity(),
                target,
                target.capacity() - target.remaining(),
                target.capacity()
        );
    }

    /**
     * Decodes a buffer using LZ4
     *
     * @param target The buffer into which the result will be written
     * @param source The compressed buffer
     * @return The size of the decompressed buffer
     */
    public static int decodeBlock(ByteBuffer target, ByteBuffer source) {
        LZ4FastDecompressor decompressor = FACTORY.fastDecompressor();
        int size = source.get();
        return decompressor.decompress(source, 4, target, 0, size);
    }

    /**
     * Gets the max compressed length for a certain buffer size
     * @param size The size of the buffer
     * @return The maximal possible length of the data after compression
     */
    public static int maxCompressedLength(int size) {
        return FACTORY.highCompressor().maxCompressedLength(size);
    }
}
