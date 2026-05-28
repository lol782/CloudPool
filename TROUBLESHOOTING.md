# CloudPool Troubleshooting Guide

## Database Connection Issues

### Problem: "Connection refused"
```
Error: could not connect to server
```

### Solution:
```bash
# Check PostgreSQL status
docker-compose ps postgres

# Check connection
psql -h localhost -U cloudpool -d cloudpool

# Review logs
docker-compose logs postgres
```

## Google Drive Integration Issues

### Problem: "Invalid credentials"
```
Error: Invalid OAuth credentials
```

### Solution:
```bash
# Verify credentials.json
ls -la credentials.json

# Check API quota
# https://console.cloud.google.com/apis/dashboard

# Regenerate credentials
# https://console.cloud.google.com/credentials
```

## Redis Connection Issues

### Problem: "REDIS_URL not set"

### Solution:
```bash
# Check Redis is running
docker-compose ps redis

# Test connection
redis-cli -h localhost ping

# Check environment
echo $REDIS_HOST
echo $REDIS_PORT
```

## Weaviate Connection Issues

### Problem: "Weaviate is not ready"

### Solution:
```bash
# Check Weaviate status
curl http://localhost:8081/v1/.well-known/ready

# View logs
docker-compose logs weaviate

# Restart Weaviate
docker-compose restart weaviate
```

## Memory Issues

### Problem: "OutOfMemoryError"

### Solution:
```bash
# Increase JVM heap
export JAVA_OPTS="-Xms512m -Xmx2g"

# Check memory usage
docker stats cloudpool-spring-boot

# Enable memory monitoring
kubectl top nodes
kubectl top pods
```

## Performance Issues

### Problem: "Slow API responses"

### Solution:
```bash
# Check slow queries
SHOW SLOW QUERIES;

# Monitor resource usage
top
docker stats

# Check cache hit rate
redis-cli INFO stats

# Review logs for errors
tail -f logs/cloudpool.log
```

## Deployment Issues

### Problem: "Pod not starting"

### Solution:
```bash
# Check pod status
kubectl describe pod cloudpool-api-xxx -n cloudpool

# View logs
kubectl logs cloudpool-api-xxx -n cloudpool

# Check resource requests
kubectl top pods -n cloudpool

# Debug node issues
kubectl describe node <node-name>
```
