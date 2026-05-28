package com.cloudpool.util;

import java.nio.charset.StandardCharsets;

public class JniTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Checking JNI Library Loading...");
        if (!RustBridge.isLibraryLoaded()) {
            System.err.println("ERROR: JNI Library not loaded!");
            System.exit(1);
        }
        System.out.println("SUCCESS: JNI Library loaded!");

        String testStr = "hello world native FFI check";
        byte[] bytes = testStr.getBytes(StandardCharsets.UTF_8);

        // 1. Test checksum
        System.out.println("Calculating checksum...");
        String checksum = RustBridge.calculateChecksum(bytes);
        System.out.println("SUCCESS: Checksum: " + checksum);
        if (checksum == null || checksum.length() != 64) {
            System.err.println("ERROR: Invalid checksum length!");
            System.exit(1);
        }

        // 2. Test compression
        System.out.println("Compressing content...");
        byte[] compressed = RustBridge.compress(bytes);
        System.out.println("SUCCESS: Compressed size: " + compressed.length + " bytes");

        // 3. Test decompression
        System.out.println("Decompressing content...");
        byte[] decompressed = RustBridge.decompress(compressed);
        String decompressedStr = new String(decompressed, StandardCharsets.UTF_8);
        System.out.println("SUCCESS: Decompressed content: \"" + decompressedStr + "\"");

        if (!testStr.equals(decompressedStr)) {
            System.err.println("ERROR: Content mismatch after decompressing!");
            System.exit(1);
        }

        System.out.println("ALL JNI BINDINGS STABLE AND WORKING 100% CORRECTLY!");
        System.out.println("==================================================");
    }
}
