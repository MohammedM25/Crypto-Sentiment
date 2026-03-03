package com.acme.crypto.trend;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Trend {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String platform;
    private String topic;
    private Double score;
    private Instant capturedAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }
}
