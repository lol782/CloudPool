# Caching Strategy

## Redis Caching Layers

### L1: Application Level
| Cache Target | TTL |
|-------------|-----|
| User metadata | 1 hour |
| File metadata | 1 hour |
| Table schemas | 24 hours |

### L2: Redis Cache
| Cache Target | TTL |
|-------------|-----|
| Query results | 1 hour |
| API responses | 30 minutes |
| User sessions | 1 day |

---

## Cache Invalidation

1. **Time-based** — TTL expiration (entries automatically expire after their TTL).
2. **Event-based** — Manual invalidation triggered on write operations (e.g., file upload invalidates the file list cache).
3. **LRU** — Least Recently Used eviction when memory limits are reached.

---

## Cache Keys

All cache keys follow a consistent naming convention:

```
file:{fileId}
bucket:{bucketId}
table:{tableId}
user:{userId}
search:{collectionId}:{query_hash}
```

This convention is enforced by `CacheService.java` which uses prefixed keys (`file:`, `table:`, `collection:`) for all Redis operations.

---

## Monitoring Cache

| Metric | Target |
|--------|--------|
| Hit rate | > 80% |
| Max memory | 2 GB |
| Eviction policy | `allkeys-lru` |

### Redis Configuration

```
maxmemory 2gb
maxmemory-policy allkeys-lru
```

### Monitoring Commands

```bash
# Check hit rate
redis-cli INFO stats | grep keyspace

# Check memory usage
redis-cli INFO memory | grep used_memory_human

# Monitor cache operations in real-time
redis-cli MONITOR
```
