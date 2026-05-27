package com.cloudpool.service;

import com.cloudpool.model.DatabaseConnection;
import com.cloudpool.service.DatabaseConsoleService.QueryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
@Slf4j
public class RedisConsoleService {

    public QueryResult executeCommand(DatabaseConnection conn, String commandText) {
        String host = conn != null ? conn.getHost() : "localhost";
        int port = conn != null ? conn.getPort() : 6379;
        String password = conn != null ? conn.getPassword() : null;

        log.info("Connecting to Redis at {}:{}", host, port);

        try (Jedis jedis = new Jedis(host, port, 2000)) { // 2s timeout
            if (password != null && !password.trim().isEmpty()) {
                jedis.auth(password);
            }

            // Simple PING to verify connection
            String pingRes = jedis.ping();
            if (!"PONG".equalsIgnoreCase(pingRes)) {
                throw new RuntimeException("Redis connection ping failed: " + pingRes);
            }

            // Parse command
            String[] tokens = commandText.trim().split("\\s+");
            if (tokens.length == 0 || tokens[0].isEmpty()) {
                throw new IllegalArgumentException("No command provided");
            }

            String cmd = tokens[0].toUpperCase();
            QueryResult result = new QueryResult();
            result.setSuccess(true);
            result.setColumns(Arrays.asList("KEY", "VALUE"));

            List<Map<String, Object>> rows = new ArrayList<>();

            switch (cmd) {
                case "PING":
                    rows.add(Map.of("KEY", "PING_RESPONSE", "VALUE", "PONG"));
                    result.setMessage("PING successful.");
                    break;
                case "SET":
                    if (tokens.length < 3) {
                        throw new IllegalArgumentException("Usage: SET <key> <value>");
                    }
                    String setKey = tokens[1];
                    // Join back the remaining tokens as value
                    String setValue = String.join(" ", Arrays.copyOfRange(tokens, 2, tokens.length));
                    String setStatus = jedis.set(setKey, setValue);
                    rows.add(Map.of("KEY", setKey, "VALUE", setStatus));
                    result.setMessage("Key set successfully.");
                    break;
                case "GET":
                    if (tokens.length < 2) {
                        throw new IllegalArgumentException("Usage: GET <key>");
                    }
                    String getKey = tokens[1];
                    String getValue = jedis.get(getKey);
                    rows.add(Map.of("KEY", getKey, "VALUE", getValue != null ? getValue : "(nil)"));
                    result.setMessage(getValue != null ? "Key retrieved successfully." : "Key not found.");
                    break;
                case "DEL":
                    if (tokens.length < 2) {
                        throw new IllegalArgumentException("Usage: DEL <key1> [key2...]");
                    }
                    long deletedCount = 0;
                    for (int i = 1; i < tokens.length; i++) {
                        deletedCount += jedis.del(tokens[i]);
                    }
                    rows.add(Map.of("KEY", "DELETED_COUNT", "VALUE", deletedCount));
                    result.setMessage("Deleted " + deletedCount + " key(s).");
                    break;
                case "KEYS":
                    String pattern = tokens.length > 1 ? tokens[1] : "*";
                    Set<String> keys = jedis.keys(pattern);
                    for (String k : keys) {
                        rows.add(Map.of("KEY", k, "VALUE", ""));
                    }
                    result.setMessage("Found " + keys.size() + " key(s) matching pattern '" + pattern + "'.");
                    break;
                case "EXISTS":
                    if (tokens.length < 2) {
                        throw new IllegalArgumentException("Usage: EXISTS <key>");
                    }
                    boolean exists = jedis.exists(tokens[1]);
                    rows.add(Map.of("KEY", tokens[1], "VALUE", exists ? "1" : "0"));
                    result.setMessage(exists ? "Key exists." : "Key does not exist.");
                    break;
                case "FLUSHDB":
                    String flushStatus = jedis.flushDB();
                    rows.add(Map.of("KEY", "STATUS", "VALUE", flushStatus));
                    result.setMessage("Database flushed successfully.");
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported Redis command '" + cmd + "'. Supported commands: PING, SET, GET, DEL, KEYS, EXISTS, FLUSHDB.");
            }

            result.setRows(rows);
            result.setAffectedRows(rows.size());
            return result;
        } catch (Exception e) {
            log.error("Redis execution failed: {}", e.getMessage(), e);
            QueryResult errorResult = new QueryResult();
            errorResult.setSuccess(false);
            errorResult.setColumns(Collections.singletonList("ERROR"));
            errorResult.setRows(Collections.singletonList(Map.of("ERROR", e.getMessage())));
            errorResult.setMessage(e.getMessage());
            return errorResult;
        }
    }
}
