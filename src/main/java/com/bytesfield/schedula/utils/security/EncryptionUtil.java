package com.bytesfield.schedula.utils.security;

import com.bytesfield.schedula.exceptions.DefaultException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    // 16 chars = 128-bit key
    private static final String SECRET;

    static {
        SECRET = System.getenv("ENCRYPTION_KEY");
        if (SECRET == null || SECRET.isEmpty()) {
            throw new IllegalStateException("ENCRYPTION_KEY environment variable is not set or is empty");
        }
    }

    private EncryptionUtil() {
    }

    public static String encrypt(String token) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(token.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Error while encrypting token: {}", e.getMessage(), e);
            throw new DefaultException("Error while encrypting token", e);
        }
    }

    public static String decrypt(String encryptedToken) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(encryptedToken);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            log.error("Error while decrypting token: {}", e.getMessage(), e);
            throw new DefaultException("Error while decrypting token", e);
        }
    }
}

