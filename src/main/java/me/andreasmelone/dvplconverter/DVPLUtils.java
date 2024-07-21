package me.andreasmelone.dvplconverter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class DVPLUtils {
    /**
     * Encodes a buffer using DVPL
     * @param buffer The buffer to encode
     * @param shouldCompress Whether the data should be compressed
     * @return A buffer containing the encoded data
     */
    public static ByteBuffer encodeDVPL(ByteBuffer buffer, boolean shouldCompress) {
        ByteBuffer output = ByteBuffer.allocate(LZ4.maxCompressedLength(buffer.capacity()));
        int compressedBlockSize = LZ4.encodeBlockHC(buffer, output);
        output.limit(compressedBlockSize);

        ByteBuffer footerBuffer;
        if (compressedBlockSize == 0 || compressedBlockSize >= buffer.capacity() || !shouldCompress) {
            // cannot or should not be compressed, or it became bigger after compressed (why compress it then?)
            footerBuffer = toDVPLFooter(buffer.capacity(), buffer.capacity(), crc32(buffer), 0);

            buffer.flip();
            return ByteBufferUtil.concat(buffer, footerBuffer);
        } else {
            output = output.slice(0, compressedBlockSize);
            footerBuffer = toDVPLFooter(buffer.capacity(), compressedBlockSize, crc32(output), 2);

            output.flip();
            return ByteBufferUtil.concat(output, footerBuffer);
        }
    }

    /**
     * Decodes a DVPL encoded buffer
     *
     * @param buffer The buffer to decode
     * @return A buffer containing the decoded data
     * @throws IOException If the data is corrupted or the format is unknown
     */
    public static ByteBuffer decodeDVPL(ByteBuffer buffer) throws IOException {
        Footer footerData = readDVPLFooter(buffer);

        // Set position to the beginning of the data block
        ByteBuffer targetBlock = buffer.slice(0, buffer.capacity() - 20);

        if (targetBlock.remaining() != footerData.cSize)
            throw new IOException("The size of the buffer does not match the size provided in the footer.");

        if (crc32(targetBlock) != footerData.crc32)
            throw new IOException("The CRC32 checksum does not match: actual checksum: " + crc32(targetBlock) + " != footer checksum: " + footerData.crc32);

        if (footerData.type == 0) {
            if (footerData.oSize != footerData.cSize) {
                throw new IOException("Size of compressed and uncompressed data does not match when no compression has been applied!");
            } else {
                targetBlock.flip();
                return targetBlock;
            }
        } else if (footerData.type == 1 || footerData.type == 2) {
            ByteBuffer deDVPLBlock = ByteBuffer.allocate(footerData.oSize);

            int decompressedBlockSize = LZ4.decodeBlock(targetBlock, deDVPLBlock);
            // I don't know what the exception message means, I didn't write it originally
            if (decompressedBlockSize != footerData.oSize) throw new IOException("The decompressed block size does not match the original size. Expected: " + footerData.oSize + ", got: " + decompressedBlockSize);

            return deDVPLBlock;
        } else {
            throw new IOException("Unkown DVPL format");
        }
    }

    /**
     * Creates a DVPL footer
     *
     * @param inputSize      The size of the input data
     * @param compressedSize The size of the compressed data
     * @param crc32          The CRC32 checksum of the data
     * @param type           The type of the compression (0 for none, 1 for LZ4, 2 for LZ4HC)
     * @return The footer buffer
     */
    public static ByteBuffer toDVPLFooter(int inputSize, int compressedSize, int crc32, int type) {
        ByteBuffer result = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        result.putInt(inputSize);
        result.putInt(compressedSize);
        result.putInt(crc32);
        result.putInt(type);
        String magicNumber = "DVPL";
        // Convert to UTF-8 encoded byte array and put directly into the buffer
        result.put(magicNumber.getBytes(StandardCharsets.UTF_8));

        // Reset the position to the beginning
        result.flip();

        return result;
    }

    /**
     * Reads the DVPL footer from the buffer
     *
     * @param buffer The buffer to read the footer from
     * @return The footer object
     */
    public static Footer readDVPLFooter(ByteBuffer buffer) {
        if (buffer.capacity() < 20) {
            throw new RuntimeException("The footer of the DVPL file is invalid as it is too small.");
        }

        ByteBuffer footerBuffer = buffer.slice(buffer.capacity() - 20, 20).order(ByteOrder.LITTLE_ENDIAN);
        //check for valid footer data
        byte[] signatureBytes = new byte[4];
        footerBuffer.get(16, signatureBytes);
        String signature = new String(signatureBytes, StandardCharsets.UTF_8);
        if (!signature.equals("DVPL")) {
            throw new RuntimeException("The signature of the footer is invalid: " + signature);
        }

        Footer footerObject = new Footer();
        footerObject.oSize = footerBuffer.getInt(0);
        footerObject.cSize = footerBuffer.getInt(4);
        footerObject.crc32 = footerBuffer.getInt(8);
        footerObject.type = footerBuffer.getInt(12);
        return footerObject;
    }

    private static int crc32(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return (int) crc.getValue();
    }

    private static int crc32(ByteBuffer buffer) {
        return crc32(ByteBufferUtil.toByteArray(buffer));
    }

    private static long readUnsignedInt(ByteBuffer buffer, int index) {
        byte[] bytes = new byte[4];
        buffer.get(index, bytes);
        long out = 0;
        for (int i = 0; i < 4; i++) {
            out |= ((long) bytes[i]) << (i * 8);
        }
        return out;
    }

    public static class Footer {
        public int oSize;
        public int cSize;
        public int crc32;
        public int type;

        public int getOriginalSize() {
            return oSize;
        }

        public int getCompressedSize() {
            return cSize;
        }

        public int getCRC32Checksum() {
            return crc32;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            return String.format("Footer={oSize=%s, cSize=%s, crc32=%s, type=%s}", oSize, cSize, crc32, type);
        }
    }
}
