package com.cloudpool.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FILE_CACHE_PREFIX = "file:";
    private static final String TABLE_CACHE_PREFIX = "table:";
    private static final String COLLECTION_CACHE_PREFIX = "collection:";
    private static final long DEFAULT_TTL = 1; // 1 hour

    /**
     * Cache file metadata
     */
    @Cacheable(value = "files", key = "#id")
    public void cacheFile(Object id, Object data) {
        String key = FILE_CACHE_PREFIX + id;
        redisTemplate.opsForValue().set(key, data, DEFAULT_TTL, TimeUnit.HOURS);
        log.debug("Cached file: {}", id);
    }

    /**
     * Get cached file
     */
    public Object getCachedFile(Object id) {
        String key = FILE_CACHE_PREFIX + id;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Invalidate file cache
     */
    @CacheEvict(value = "files", key = "#id")
    public void invalidateFile(Object id) {
        String key = FILE_CACHE_PREFIX + id;
        redisTemplate.delete(key);
        log.debug("Invalidated file cache: {}", id);
    }

    /**
     * Cache table metadata
     */
    public void cacheTable(Object id, Object data) {
        String key = TABLE_CACHE_PREFIX + id;
        redisTemplate.opsForValue().set(key, data, DEFAULT_TTL, TimeUnit.HOURS);
    }

    /**
     * Cache collection
     */
    public void cacheCollection(Object id, Object data) {
        String key = COLLECTION_CACHE_PREFIX + id;
        redisTemplate.opsForValue().set(key, data, DEFAULT_TTL, TimeUnit.HOURS);
    }

    /**
     * Clear all caches
     */
    public void clearAll() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        log.info("All caches cleared");
    }
}
