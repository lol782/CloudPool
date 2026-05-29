package com.cloudpool.util;
 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
 
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
 
@Component
@Slf4j
public class EncryptionUtil {
 
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private final SecretKey secretKey;
 
    public EncryptionUtil(@Value("${cloudpool.encryption.master-key:d1f88c8078c1db294e82b71be5e8f6e80b2a75ffca79b9e6e6a1a8c3d6e5a6b0c2e3f4g5h6j7k8l9m0n1p2q3r4s5t6u7v8w9x0y1z2a3b4c5d6e7f8g9}") String masterKeyBase64) {
        try {
            // Strip whitespace/newlines
            String cleanKey = masterKeyBase64.trim();
            byte[] decodedKey;
            
            // Try standard base64 decoding first, fallback to raw bytes if it's not base64
            try {
                decodedKey = Base64.getDecoder().decode(cleanKey);
            } catch (IllegalArgumentException e) {
                // If it's a raw password/key string, pad/truncate to 32 bytes
                byte[] rawBytes = cleanKey.getBytes(StandardCharsets.UTF_8);
                decodedKey = new byte[32];
                System.arraycopy(rawBytes, 0, decodedKey, 0, Math.min(rawBytes.length, 32));
            }
 
            if (decodedKey.length != 32) { // 256 bits = 32 bytes
                // Pad/truncate key to 32 bytes for safety
                byte[] temp = new byte[32];
                System.arraycopy(decodedKey, 0, temp, 0, Math.min(decodedKey.length, 32));
                decodedKey = temp;
            }
 
            this.secretKey = new SecretKeySpec(decodedKey, 0, 32, ALGORITHM);
            log.info("✅ Encryption key initialized successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption master key config", e);
        }
    }
 
    /**
     * Encrypt plaintext
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            return null;
        }
        
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage());
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }
 
    /**
     * Decrypt ciphertext
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return null;
        }
        
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Invalid base64 in ciphertext");
            throw new RuntimeException("Invalid encrypted data format", e);
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
 
    /**
     * Generate a new master key (run once during setup)
     */
    public static String generateMasterKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE);
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate master key", e);
        }
    }
}
