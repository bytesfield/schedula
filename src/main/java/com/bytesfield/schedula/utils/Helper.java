package com.bytesfield.schedula.utils;

import com.bytesfield.schedula.exceptions.DefaultException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.concurrent.Callable;

@Slf4j
public class Helper {
    private Helper() {
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new DefaultException("Error hashing password", e);
        }
    }

    public static <T> T retryWithBackoff(Callable<T> task, int maxRetries, long delayMs) throws Exception {
        int attempt = 0;

        while (true) {
            try {
                attempt++;
                return task.call();
            } catch (Exception e) {
                if (!isRetryable(e) || attempt >= maxRetries) {
                    throw e;
                }

                long backoffTime = delayMs * (long) Math.pow(2, (double) attempt - 1);
                String message = String.format(
                        "Attempt %d failed: %s. Backing off for %d ms before retrying...",
                        attempt, e.getMessage(), backoffTime
                );
                log.warn(message);

                try {
                    Thread.sleep(backoffTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new DefaultException("Retry interrupted", ie);
                }
            }
        }
    }

    private static boolean isRetryable(Exception ex) {
        // Feign 5xx errors or timeouts are retryable
        if (ex instanceof feign.FeignException feignEx) {
            int status = feignEx.status();
            return status >= 500 && status < 600;
        }

        // Network/IO issues
        return ex instanceof java.io.IOException;
    }


    public static long secondsToMilliseconds(long seconds) {
        return seconds * 1000L;
    }

    public static String generateUniqueCharacters(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();

        if (length < 1) throw new IllegalArgumentException("Length must be at least 1");

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
