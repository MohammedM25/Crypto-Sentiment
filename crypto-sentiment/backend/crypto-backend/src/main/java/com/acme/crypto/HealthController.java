package com.acme.crypto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "service", "crypto-backend",
            "message", "Service is running",
            "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "OK");
        health.put("service", "crypto-backend");
        health.put("timestamp", Instant.now().toString());

        if (dataSource != null) {
            health.put("database", testDatabaseConnection());
        } else {
            health.put("database", Map.of(
                "status", "N/A",
                "message", "No database configured"
            ));
        }

        return ResponseEntity.ok(health);
    }

    @GetMapping("/test-db")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        Map<String, Object> result = new HashMap<>();

        if (dataSource == null) {
            result.put("status", "ERROR");
            result.put("message", "No database configured");
            return ResponseEntity.ok(result);
        }

        try (Connection connection = dataSource.getConnection()) {
            result.put("connection", "OK");
            result.put("url", connection.getMetaData().getURL());
            result.put("driver", connection.getMetaData().getDriverName());

            try (var stmt = connection.createStatement()) {
                var rs = stmt.executeQuery("SELECT COUNT(*) FROM trend");
                if (rs.next()) {
                    result.put("trend_count", rs.getInt(1));
                }
                result.put("query_test", "OK");
            } catch (SQLException e) {
                result.put("query_test", "FAILED");
                result.put("query_error", e.getMessage());
            }

            result.put("status", "OK");

        } catch (SQLException e) {
            result.put("status", "ERROR");
            result.put("message", "Database connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            result.put("status", "UP");
            result.put("message", "Database connection successful");
            result.put("url", connection.getMetaData().getURL());
            result.put("driver", connection.getMetaData().getDriverName());
        } catch (SQLException e) {
            result.put("status", "DOWN");
            result.put("message", "Database connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        return result;
    }

    @GetMapping("/api/test-data")
    public ResponseEntity<Map<String, Object>> getTestData() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Data comes from news RSS feeds. Visit /api/scrape to fetch latest.");
        data.put("timestamp", Instant.now().toString());
        data.put("status", "success");
        return ResponseEntity.ok(data);
    }
}
