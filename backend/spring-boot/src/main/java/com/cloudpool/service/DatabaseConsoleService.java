package com.cloudpool.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSetMetaData;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DatabaseConsoleService {

    private final JdbcTemplate jdbcTemplate;
    private final MetricsService metricsService;

    private JdbcTemplate getJdbcTemplateForConnection(com.cloudpool.model.DatabaseConnection conn) {
        if (conn == null) {
            return jdbcTemplate;
        }
        try {
            org.springframework.jdbc.datasource.DriverManagerDataSource dataSource = new org.springframework.jdbc.datasource.DriverManagerDataSource();
            if ("POSTGRESQL".equalsIgnoreCase(conn.getDbType())) {
                dataSource.setDriverClassName("org.postgresql.Driver");
                dataSource.setUrl("jdbc:postgresql://" + conn.getHost() + ":" + conn.getPort() + "/" + conn.getDatabaseName());
            } else {
                return jdbcTemplate;
            }
            dataSource.setUsername(conn.getUsername());
            dataSource.setPassword(conn.getPassword());
            return new JdbcTemplate(dataSource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct dynamic PostgreSQL connection: " + e.getMessage(), e);
        }
    }

    private void validateH2Query(String sql, com.cloudpool.model.User user) {
        if (user == null) {
            throw new SecurityException("Unauthorized SQL query execution");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return; // Admin can execute anything on local H2
        }
        
        String cleanSql = sql.toLowerCase().trim();
        
        // List of prohibited system/metadata table names
        List<String> prohibitedTables = Arrays.asList(
            "users", "database_connections", "buckets", "file_metadata", 
            "background_jobs", "api_keys", "flyway_schema_history", 
            "dev_tables", "dev_table_fields", "vector_collections", 
            "vector_documents", "api_key_usage_logs", "information_schema", 
            "pg_"
        );
        
        for (String table : prohibitedTables) {
            String regex = "\\b" + java.util.regex.Pattern.quote(table) + "\\b";
            if (java.util.regex.Pattern.compile(regex).matcher(cleanSql).find()) {
                throw new SecurityException("Access denied: Accessing system metadata table '" + table + "' is prohibited.");
            }
        }
        
        // Verify that any table referenced with "dev_tbl_" strictly belongs to this user
        String userIdStr = user.getId().toString().replace("-", "_");
        String expectedPrefix = "dev_tbl_" + userIdStr + "_";
        
        java.util.regex.Pattern devTblPattern = java.util.regex.Pattern.compile("\\bdev_tbl_[a-zA-Z0-9_]*\\b");
        var matcher = devTblPattern.matcher(cleanSql);
        boolean foundAny = false;
        while (matcher.find()) {
            foundAny = true;
            String tableName = matcher.group();
            if (!tableName.startsWith(expectedPrefix)) {
                throw new SecurityException("Access denied: You are not allowed to access table '" + tableName + "'.");
            }
        }
        
        boolean isHarmless = cleanSql.matches("(?i)^select\\s+\\d+(\\s+as\\s+\\w+)?$") 
            || cleanSql.startsWith("show") 
            || cleanSql.startsWith("pragma") 
            || cleanSql.startsWith("explain");
        
        if (!isHarmless && !foundAny) {
            if (cleanSql.contains("select") || cleanSql.contains("insert") || cleanSql.contains("update") || cleanSql.contains("delete") || cleanSql.contains("drop") || cleanSql.contains("create")) {
                throw new SecurityException("Access denied: SQL console queries on H2 must specify a dynamic table belonging to your account.");
            }
        }
    }

    public QueryResult executeQuery(String sql) {
        return executeQuery(sql, null, null);
    }

    public QueryResult executeQuery(String sql, com.cloudpool.model.DatabaseConnection conn, com.cloudpool.model.User user) {
        long startTime = System.currentTimeMillis();
        QueryResult result;
        try {
            if (conn == null) {
                try {
                    validateH2Query(sql, user);
                } catch (SecurityException e) {
                    QueryResult errResult = new QueryResult();
                    errResult.setSuccess(false);
                    errResult.setColumns(Collections.singletonList("ERROR"));
                    errResult.setRows(Collections.singletonList(Map.of("ERROR", e.getMessage())));
                    errResult.setMessage(e.getMessage());
                    return errResult;
                }
            }

            String cleanSql = sql.trim().toUpperCase();
            JdbcTemplate targetTemplate = getJdbcTemplateForConnection(conn);
            
            if (cleanSql.startsWith("SELECT") || cleanSql.startsWith("SHOW") || cleanSql.startsWith("PRAGMA") || cleanSql.startsWith("EXPLAIN")) {
                result = targetTemplate.query(sql, rs -> {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metaData.getColumnName(i));
                    }
                    
                    List<Map<String, Object>> rows = new ArrayList<>();
                    int rowCount = 0;
                    while (rs.next() && rowCount < 100) { // Limit to 100 rows for display safety
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }
                        rows.add(row);
                        rowCount++;
                    }
                    
                    QueryResult res = new QueryResult();
                    res.setSuccess(true);
                    res.setColumns(columns);
                    res.setRows(rows);
                    res.setAffectedRows(rowCount);
                    res.setMessage("Query executed successfully. Returned " + rowCount + " rows.");
                    return res;
                });
            } else {
                int affectedRows = targetTemplate.update(sql);
                QueryResult okResult = new QueryResult();
                okResult.setSuccess(true);
                okResult.setColumns(Collections.singletonList("STATUS"));
                okResult.setRows(Collections.singletonList(Map.of("STATUS", "SUCCESS")));
                okResult.setAffectedRows(affectedRows);
                okResult.setMessage("Query executed successfully. Affected rows: " + affectedRows);
                result = okResult;
            }
        } catch (Exception e) {
            QueryResult errResult = new QueryResult();
            errResult.setSuccess(false);
            errResult.setColumns(Collections.singletonList("ERROR"));
            errResult.setRows(Collections.singletonList(Map.of("ERROR", e.getMessage())));
            errResult.setMessage(e.getMessage());
            result = errResult;
        } finally {
            metricsService.recordQueryTime(System.currentTimeMillis() - startTime);
        }
        return result;
    }

    @Data
    public static class QueryResult {
        private boolean success;
        private List<String> columns = new ArrayList<>();
        private List<Map<String, Object>> rows = new ArrayList<>();
        private int affectedRows;
        private String message;
    }
}
