package com.acme.crypto.trend;

import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.DoubleSummaryStatistics;
import java.util.Comparator;

@Service
@ConditionalOnProperty(name = "trend.service.enabled", havingValue = "true", matchIfMissing = false)
public class TrendService {
    
    private final TrendRepository repo;

    public TrendService(TrendRepository repo) {
        this.repo = repo;
    }

    public List<Trend> all() { 
        return repo.findAll(); 
    }
    
    public Trend get(Long id) { 
        return repo.findById(id).orElseThrow(); 
    }
    
    public List<Trend> byPlatform(String platform) { 
        return repo.findByPlatformIgnoreCase(platform); 
    }
    
    public List<Trend> byTopic(String topic) {
        return repo.findByTopicIgnoreCase(topic);
    }
    
    public List<Trend> getRecentTrends(Instant cutoff) {
        return repo.findByCapturedAtAfter(cutoff);
    }

    public Map<String, Object> getTrendingCoins(Instant cutoff) {
        List<Trend> recentTrends = repo.findByCapturedAtAfter(cutoff);
        
        if (recentTrends.isEmpty()) {
            Map<String, Object> emptyTrending = new HashMap<>();
            emptyTrending.put("trendingCoins", List.of());
            emptyTrending.put("totalMentions", 0);
            emptyTrending.put("timeWindow", "6 hours");
            return emptyTrending;
        }
        
        Map<String, List<Trend>> trendsByTopic = recentTrends.stream()
            .collect(Collectors.groupingBy(Trend::getTopic));
        
        List<Map<String, Object>> trendingCoins = trendsByTopic.entrySet().stream()
            .map(entry -> {
                String topic = entry.getKey();
                List<Trend> topicTrends = entry.getValue();
                
                int mentionCount = topicTrends.size();
                double avgSentiment = topicTrends.stream()
                    .mapToDouble(Trend::getScore)
                    .average()
                    .orElse(0.0);
                
                List<Trend> sortedTrends = topicTrends.stream()
                    .sorted(Comparator.comparing(Trend::getCapturedAt))
                    .collect(Collectors.toList());
                
                double sentimentTrend = 0.0;
                if (sortedTrends.size() >= 2) {
                    int midPoint = sortedTrends.size() / 2;
                    double recentAvg = sortedTrends.subList(midPoint, sortedTrends.size()).stream()
                        .mapToDouble(Trend::getScore)
                        .average()
                        .orElse(0.0);
                    double olderAvg = sortedTrends.subList(0, midPoint).stream()
                        .mapToDouble(Trend::getScore)
                        .average()
                        .orElse(0.0);
                    sentimentTrend = recentAvg - olderAvg;
                }
                
                long platformCount = topicTrends.stream()
                    .map(Trend::getPlatform)
                    .distinct()
                    .count();
                
                double trendingScore = (mentionCount * 0.4) + 
                                     (avgSentiment * 0.3) + 
                                     (sentimentTrend * 10 * 0.2) + 
                                     (platformCount * 0.1);
                
                Map<String, Object> coinData = new HashMap<>();
                coinData.put("symbol", topic);
                coinData.put("mentionCount", mentionCount);
                coinData.put("avgSentiment", Math.round(avgSentiment * 100.0) / 100.0);
                coinData.put("sentimentTrend", Math.round(sentimentTrend * 100.0) / 100.0);
                coinData.put("platformCount", platformCount);
                coinData.put("trendingScore", Math.round(trendingScore * 100.0) / 100.0);
                coinData.put("platforms", topicTrends.stream()
                    .collect(Collectors.groupingBy(Trend::getPlatform, Collectors.counting())));
                
                return coinData;
            })
            .sorted(Comparator.comparing((Map<String, Object> coin) -> 
                (Double) coin.get("trendingScore")).reversed())
            .limit(20)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("trendingCoins", trendingCoins);
        result.put("totalMentions", recentTrends.size());
        result.put("timeWindow", "6 hours");
        result.put("lastUpdated", Instant.now());
        
        return result;
    }
    
    public Trend saveReal(String platform, String topic, double score) {
        Trend t = new Trend();
        t.setPlatform(platform);
        t.setTopic(topic);
        t.setScore(score);
        t.setCapturedAt(Instant.now());
        return repo.save(t);
    }
    
    
    public Map<String, Object> getTrendsSummary() {
        List<Trend> allTrends = repo.findAll();
        
        if (allTrends.isEmpty()) {
            Map<String, Object> emptySummary = new HashMap<>();
            emptySummary.put("totalPosts", 0);
            emptySummary.put("averageScore", 0.0);
            emptySummary.put("platforms", Map.of());
            emptySummary.put("topics", Map.of());
            return emptySummary;
        }
        
        DoubleSummaryStatistics stats = allTrends.stream()
            .mapToDouble(Trend::getScore)
            .summaryStatistics();
        
        Map<String, Long> platformCounts = allTrends.stream()
            .collect(Collectors.groupingBy(Trend::getPlatform, Collectors.counting()));
        
        Map<String, Long> topicCounts = allTrends.stream()
            .collect(Collectors.groupingBy(Trend::getTopic, Collectors.counting()));
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPosts", allTrends.size());
        summary.put("averageScore", Math.round(stats.getAverage() * 100.0) / 100.0);
        summary.put("minScore", stats.getMin());
        summary.put("maxScore", stats.getMax());
        summary.put("platforms", platformCounts);
        summary.put("topics", topicCounts);
        summary.put("lastUpdated", Instant.now());
        
        return summary;
    }
    
    public void clearAllTrends() {
        repo.deleteAll();
    }
    
    public void clearTrendsByTopic(String topic) {
        repo.deleteByTopicIgnoreCase(topic);
    }

    public int seedDemoData() {
        String[] platforms = {"twitter", "reddit", "telegram"};
        String[] topics = {"BTC", "ETH", "SOL", "DOGE", "XRP", "ADA"};
        int count = 0;
        Instant base = Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS);
        for (int i = 0; i < 30; i++) {
            String platform = platforms[i % platforms.length];
            String topic = topics[i % topics.length];
            double score = -0.5 + Math.random() * 1.5;
            Trend t = new Trend();
            t.setPlatform(platform);
            t.setTopic(topic);
            t.setScore(Math.round(score * 100.0) / 100.0);
            t.setCapturedAt(base.plus(i * 48, java.time.temporal.ChronoUnit.MINUTES));
            repo.save(t);
            count++;
        }
        return count;
    }

}
