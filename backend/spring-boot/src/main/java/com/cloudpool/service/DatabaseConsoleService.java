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

    public QueryResult executeQuery(String sql) {
        return executeQuery(sql, null);
    }

    public QueryResult executeQuery(String sql, com.cloudpool.model.DatabaseConnection conn) {
        String cleanSql = sql.trim().toUpperCase();
        JdbcTemplate targetTemplate = getJdbcTemplateForConnection(conn);
        
        try {
            if (cleanSql.startsWith("SELECT") || cleanSql.startsWith("SHOW") || cleanSql.startsWith("PRAGMA") || cleanSql.startsWith("EXPLAIN")) {
                return targetTemplate.query(sql, rs -> {
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
                    
                    QueryResult result = new QueryResult();
                    result.setSuccess(true);
                    result.setColumns(columns);
                    result.setRows(rows);
                    result.setAffectedRows(rowCount);
                    result.setMessage("Query executed successfully. Returned " + rowCount + " rows.");
                    return result;
                });
            } else {
                int affectedRows = targetTemplate.update(sql);
                QueryResult result = new QueryResult();
                result.setSuccess(true);
                result.setColumns(Collections.singletonList("STATUS"));
                result.setRows(Collections.singletonList(Map.of("STATUS", "SUCCESS")));
                result.setAffectedRows(affectedRows);
                result.setMessage("Query executed successfully. Affected rows: " + affectedRows);
                return result;
            }
        } catch (Exception e) {
            QueryResult result = new QueryResult();
            result.setSuccess(false);
            result.setColumns(Collections.singletonList("ERROR"));
            result.setRows(Collections.singletonList(Map.of("ERROR", e.getMessage())));
            result.setMessage(e.getMessage());
            return result;
        }
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
