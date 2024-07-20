# DVPLConverter
DVPLConverter is a tool written in java to encode files to DVPL and decode DVPL files to original files.<br>
This tool is a port of https://github.com/Maddoxkkm/dvpl_converter to java. All the info here is taken from there.

## What is DVPL?
DVPL is a format first seen in a chinese client of the game "World of Tanks Blitz"<br>
Now it is used in all versions of the game. This only includes files in the Android/data directory of Blitz.<br>
The files inside the game APK are not encoded in this format.

## What is this tool useful for?
This tool is useful for modding the game or looking at its files.<br>
I found the JS version to be somewhat challenging to install, so I decided to port it over to java.

## Specification
This is copied from the previously mentioned repository.
- Starts with stream of Byte data, can be compressed or uncompressed.
- The last 20 bytes in DVPL files are in the following format:
    - UINT32LE input size in Byte
    - UINT32LE compressed block size in Byte
    - UINT32LE compressed block crc32
    - UINT32LE compression Type
        - 0: no compression (format used in all uncompressed `.dvpl` files from SmartDLC)
        - 1: LZ4 (not observed but handled by this decompressor)
        - 2: LZ4_HC (format used in all compressed `.dvpl` files from SmartDLC)
        - 3: RFC1951 (not implemented in this decompressor since it's not observed)
    - 32-bit Magic Number represents "DVPL" literals in utf8 encoding, encoded in big-Endian.      
