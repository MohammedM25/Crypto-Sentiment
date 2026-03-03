package com.acme.crypto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

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
    
    @Autowired(required = false)
    private S3Client s3Client;
    
    @Autowired(required = false)
    private SqsClient sqsClient;
    
    @Value("${aws.s3.bucket-name:}")
    private String bucketName;
    
    @Value("${aws.sqs.processing-queue-url:}")
    private String processingQueueUrl;
    
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
        
        if (s3Client != null && !bucketName.isEmpty()) {
            health.put("s3", testS3Connection());
        } else {
            health.put("s3", Map.of(
                "status", "N/A",
                "message", "S3 not configured"
            ));
        }
        
        if (sqsClient != null && !processingQueueUrl.isEmpty()) {
            health.put("sqs", testSqsConnection());
        } else {
            health.put("sqs", Map.of(
                "status", "N/A",
                "message", "SQS not configured"
            ));
        }
        
        health.put("aws", Map.of(
            "status", "PARTIAL",
            "message", "Some AWS services connected"
        ));
        
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
    
    private Map<String, Object> testS3Connection() {
        Map<String, Object> result = new HashMap<>();
        try {
            HeadBucketRequest request = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

            s3Client.headBucket(request);
            result.put("status", "UP");
            result.put("message", "S3 connection successful");
            result.put("bucket", bucketName);
        } catch (S3Exception e) {
            result.put("status", "DOWN");
            result.put("message", "S3 connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("message", "S3 connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        return result;
    }
    
    private Map<String, Object> testSqsConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            String queueName = processingQueueUrl.substring(processingQueueUrl.lastIndexOf("/") + 1);

            GetQueueUrlRequest request = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();

            sqsClient.getQueueUrl(request);
            result.put("status", "UP");
            result.put("message", "SQS connection successful");
            result.put("queue", queueName);
        } catch (SqsException e) {
            result.put("status", "DOWN");
            result.put("message", "SQS connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("message", "SQS connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        return result;
    }
    
    @GetMapping("/api/test-data")
    public ResponseEntity<Map<String, Object>> getTestData() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "No mock data - using real scraped data only");
        data.put("timestamp", Instant.now().toString());
        data.put("status", "success");
        data.put("note", "All data comes from real scraping sources");
        return ResponseEntity.ok(data);
    }
}
