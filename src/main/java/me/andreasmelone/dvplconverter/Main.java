package me.andreasmelone.dvplconverter;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("mode").withRequiredArg().ofType(String.class).required();
        parser.accepts("in").withRequiredArg().ofType(File.class).required();
        parser.accepts("out").withRequiredArg().ofType(File.class).required();
        parser.accepts("help").forHelp();

        OptionSet options;
        try {
            options = parser.parse(args);
        } catch (Exception e) {
            System.out.println("Error parsing arguments: " + e.getMessage());
            printHelp();
            System.exit(1);
            return;
        }

        if (options.has("help")) {
            printHelp();
            System.exit(0);
        }

        Mode mode = Mode.getByName((String) options.valueOf("mode"));
        if (mode == null) {
            System.out.println("Invalid mode: " + options.valueOf("mode"));
            printHelp();
            System.exit(1);
            return;
        }

        File inFile = (File) options.valueOf("in");
        File outFile = (File) options.valueOf("out");

        try {
            switch (mode) {
                case ENCODE:
                    ByteBuffer encoded = DVPLUtils.encodeDVPL(ByteBufferUtil.wrapFile(inFile), false);
                    ByteBufferUtil.writeToFile(encoded, outFile);
                    break;
                case DECODE:
                    ByteBuffer decoded = DVPLUtils.decodeDVPL(ByteBufferUtil.wrapFile(inFile));
                    ByteBufferUtil.writeToFile(decoded, outFile);
                    break;
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("Usage: java -jar dvpltool.jar --mode <mode> --in <input file> --out <output file>");
        System.out.print("Modes: ");
        for(int i = 0; i < Mode.values().length; i++) {
            System.out.print(Mode.values()[i].name().toLowerCase());
            if (i < Mode.values().length - 1)
                System.out.print(", ");
        }
        System.out.println();
    }

    public enum Mode {
        ENCODE,
        DECODE;

        public static Mode getByName(String name) {
            for (Mode mode : Mode.values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
            return null;
        }
    }
}
