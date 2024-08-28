# DVPLConverter
DVPLConverter is a tool written in java to encode files to DVPL and decode DVPL files to original files.<br>
This tool is a port of https://github.com/Maddoxkkm/dvpl_converter to java. All the info here is taken from there.

## What is DVPL?
DVPL is a format first seen in a chinese client of the game "World of Tanks Blitz"<br>
Now it is used in all versions of the game. This only includes files in the Android/data directory of Blitz.<br>
The files inside the game APK are not encoded in this format.

## What is this tool useful for?
This tool is useful for modding the game or looking at its files.<br>
I found the JS version to be somewhat challenging to install, due to the requirements of native libraries, so I decided to port it over to java.

## Specification
This is sourced from the previously mentioned repository.
- The input is a stream of binary data, which may be either compressed or uncompressed.
- The last 20 bytes in DVPL files follow this structure:
    - `UINT32LE` representing the original size of the binary data
    - `UINT32LE` representing the compressed size of the binary data
    - `UINT32LE` representing the CRC32 checksum of the binary data
    - `UINT32LE` representing the compression type:
        - 0: No compression (used in all uncompressed `.dvpl` files from SmartDLC)
        - 1: LZ4 (not observed)
        - 2: LZ4_HC (used in all compressed `.dvpl` files from SmartDLC)
        - 3: RFC1951 (not implemented in this decompressor as it is not observed)
    - A 32-bit magic number representing the UTF-8 encoded string "DVPL", stored in big-endian format.
