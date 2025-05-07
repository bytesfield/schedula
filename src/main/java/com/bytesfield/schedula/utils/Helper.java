package com.bytesfield.schedula.utils;

import com.bytesfield.schedula.exceptions.DefaultException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
                if (attempt >= maxRetries) {
                    throw e;
                }

                long backoffTime = delayMs * (long) Math.pow(2, (double) attempt - 1);

                String message = String.format("Attempt %d failed: %s. Backing off for %d ms before retrying...", attempt, e.getMessage(), backoffTime);

                log.error(message);

                try {
                    Thread.sleep(backoffTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();

                    throw new DefaultException("Retry interrupted", ie);
                }
            }
        }
    }

    public static long secondsToMilliseconds(long seconds) {
        return seconds * 1000L;
    }
}
