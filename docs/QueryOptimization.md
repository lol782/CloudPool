# Database Query Optimization

## Indexing Strategy

### Primary Indexes
- `files(bucket_id)`
- `files(created_at)`
- `files(drive_file_id)`
- `table_records(table_id)`
- `vector_documents(collection_id)`

### Composite Indexes
- `users(email, is_active)`
- `files(bucket_id, deleted_at)`
- `table_records(table_id, created_at)`

### Full-text Indexes
- `files(name)` — for file search

---

## Query Optimization Tips

1. **Use pagination** — Always limit result sets with `LIMIT` / `OFFSET`.
2. **Select specific columns** — Avoid `SELECT *`; fetch only the fields you need.
3. **Use prepared statements** — Prevent SQL injection and allow plan caching.
4. **Batch operations** — Group inserts and updates to reduce round-trips.
5. **Monitor slow queries** — Enable query logging and review execution plans.

---

## Example Optimized Queries

```sql
-- Good: Specific columns, limit
SELECT id, name, size FROM files 
WHERE bucket_id = $1 AND deleted_at IS NULL
LIMIT 20 OFFSET 0;

-- Bad: SELECT *, no limit
SELECT * FROM files WHERE bucket_id = $1;
```

---

## Connection Pooling

| Setting | Value |
|---------|-------|
| Min connections | 5 |
| Max connections | 20 |
| Connection timeout | 20s |
| Max lifetime | 20 minutes |

These values are configured via HikariCP in `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 20000
      max-lifetime: 1200000
```
