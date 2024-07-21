package me.andreasmelone.dvplconverter;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LZ4 {
    private final static LZ4Factory FACTORY = LZ4Factory.fastestInstance();

    /**
     * Encodes a buffer using LZ4HC
     *
     * @param source The uncompressed buffer, aka the input
     * @param target The buffer into which the result will be written, aka the output
     * @return The size of the compressed buffer
     */
    public static int encodeBlockHC(ByteBuffer source, ByteBuffer target) {
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
     * @param source The compressed buffer, aka the input
     * @param target The buffer into which the result will be written, aka the output
     * @return The size of the decompressed buffer
     */
    public static int decodeBlock(ByteBuffer source, ByteBuffer target) {
        LZ4SafeDecompressor decompressor = FACTORY.safeDecompressor();
        return decompressor.decompress(source, 0, source.capacity(), target, 0, target.capacity());
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
