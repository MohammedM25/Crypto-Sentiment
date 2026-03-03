package com.acme.crypto.aws;

import com.acme.crypto.trend.Trend;
import com.acme.crypto.trend.TrendRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "sentiment.processor.enabled", havingValue = "true", matchIfMissing = false)
public class SentimentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SentimentProcessor.class);

    @Autowired
    private TrendRepository trendRepository;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SqsListener("crypto-sentiment-processing-queue")
    public void processSentimentData(String message) {
        int maxRetries = 3;
        int retryDelay = 1000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("Processing SQS message (attempt {}/{}): {}", attempt, maxRetries, message.length() > 100 ? message.substring(0, 100) + "..." : message);
                
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = objectMapper.readValue(message, Map.class);
                    processIndividualSentiment(data);
                    logger.debug("Processed individual sentiment item");
                    return;
                } catch (Exception e) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> sentimentData = objectMapper.readValue(message, List.class);
                    
                    for (Map<String, Object> data : sentimentData) {
                        processIndividualSentiment(data);
                    }
                    
                    logger.info("Processed {} sentiment items from batch", sentimentData.size());
                    return;
                }
                
            } catch (Exception e) {
                logger.error("Error processing sentiment data (attempt {}/{}): {}", attempt, maxRetries, e.getMessage(), e);
                
                if (attempt == maxRetries) {
                    logger.error("Failed to process sentiment data after {} attempts. Giving up.", maxRetries);
                    return;
                }
                
                try {
                    Thread.sleep(retryDelay * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("Retry interrupted", ie);
                    return;
                }
            }
        }
    }

    private void processIndividualSentiment(Map<String, Object> data) {
        try {
            String platform = (String) data.get("platform");
            Double sentiment = (Double) data.get("sentiment");
            String mentionedCrypto = (String) data.get("mentioned_crypto");
            
            Trend trend = new Trend();
            trend.setTopic(mentionedCrypto != null ? mentionedCrypto : "GENERAL");
            trend.setScore(sentiment != null ? sentiment : 0.0);
            trend.setCapturedAt(Instant.now());
            trend.setPlatform(platform);
            
            trendRepository.save(trend);
            storeInS3(data);
            
            logger.debug("Processed sentiment for {}: {}", mentionedCrypto, sentiment);
            
        } catch (Exception e) {
            logger.error("Error processing individual sentiment: {}", e.getMessage(), e);
        }
    }

    private void storeInS3(Map<String, Object> data) {
        try {
            String key = "sentiment-data/" + Instant.now().toEpochMilli() + ".json";
            String jsonData = objectMapper.writeValueAsString(data);
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .build();
            
            s3Client.putObject(putObjectRequest, 
                software.amazon.awssdk.core.sync.RequestBody.fromString(jsonData));
            
            logger.debug("Stored data in S3: {}", key);
            
        } catch (Exception e) {
            logger.error("Error storing data in S3: {}", e.getMessage(), e);
        }
    }
}