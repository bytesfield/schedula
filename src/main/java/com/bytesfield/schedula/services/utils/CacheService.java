package com.bytesfield.schedula.services.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * CacheService provides utility methods to interact with a Redis cache.
 * It allows storing, retrieving, deleting, and managing cache entries with or without expiration times.
 */
@Component
@RequiredArgsConstructor
public class CacheService {
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * Stores a value in the cache with the specified key.
     *
     * @param key   the key under which the value will be stored
     * @param value the value to store in the cache
     */
    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Retrieves a value from the cache by its key.
     *
     * @param key the key of the value to retrieve
     * @return the value associated with the key, or null if the key does not exist
     */
    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Deletes a value from the cache by its key.
     *
     * @param key the key of the value to delete
     */
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Checks if a key exists in the cache.
     *
     * @param key the key to check for existence
     * @return true if the key exists, false otherwise
     */
    public boolean isKeyExists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * Stores a value in the cache with the specified key and an expiration time.
     *
     * @param key     the key under which the value will be stored
     * @param value   the value to store in the cache
     * @param timeout the expiration time for the key
     * @param unit    the time unit of the expiration time
     */
    public void setValueWithExpiration(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * Retrieves the expiration time of a key in the cache.
     *
     * @param key the key whose expiration time is to be retrieved
     * @return the expiration time in the specified time unit, or null if the key does not exist
     */
    public Long getKeyExpiration(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * Stores a value in the cache with the specified key and a default expiration time of 60 seconds.
     *
     * @param key   the key under which the value will be stored
     * @param value the value to store in the cache
     */
    public void setValueWithDefaultExpiration(String key, String value) {
        redisTemplate.opsForValue().set(key, value, 60, TimeUnit.SECONDS);
    }
}