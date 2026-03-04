package com.acme.crypto.trend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "trend.controller.enabled", havingValue = "true", matchIfMissing = false)
public class TrendController {
    private final TrendService svc;

    public TrendController(TrendService svc) {
        this.svc = svc;
    }

    @GetMapping("/trends")
    public List<Trend> getTrends() { 
        return svc.all(); 
    }

    @GetMapping("/trends/{id}")
    public Trend getTrend(@PathVariable Long id) { 
        return svc.get(id); 
    }

    @GetMapping("/trends/platform/{platform}")
    public List<Trend> byPlatform(@PathVariable String platform) { 
        return svc.byPlatform(platform); 
    }

    @GetMapping("/trends/topic/{topic}")
    public List<Trend> byTopic(@PathVariable String topic) {
        return svc.byTopic(topic);
    }

    @GetMapping("/trends/recent")
    public List<Trend> getRecentTrends(@RequestParam(defaultValue = "24") int hours) {
        Instant cutoff = Instant.now().minus(hours, ChronoUnit.HOURS);
        return svc.getRecentTrends(cutoff);
    }

    @GetMapping("/trends/summary")
    public Map<String, Object> getTrendsSummary() {
        return svc.getTrendsSummary();
    }

    @GetMapping("/trends/trending")
    public Map<String, Object> getTrendingCoins(@RequestParam(defaultValue = "6") int hours) {
        Instant cutoff = Instant.now().minus(hours, ChronoUnit.HOURS);
        return svc.getTrendingCoins(cutoff);
    }
    

    @DeleteMapping("/trends/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllTrends() {
        svc.clearAllTrends();
        return ResponseEntity.ok(Map.of(
            "message", "All trends cleared successfully",
            "timestamp", Instant.now().toString()
        ));
    }
    
    @DeleteMapping("/trends/topic/{topic}")
    public ResponseEntity<Map<String, Object>> clearTrendsByTopic(@PathVariable String topic) {
        svc.clearTrendsByTopic(topic);
        return ResponseEntity.ok(Map.of(
            "message", "Trends for topic '" + topic + "' cleared successfully",
            "timestamp", Instant.now().toString()
        ));
    }

    @RequestMapping(value = "/trends/seed", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> seedDemoData() {
        int count = svc.seedDemoData();
        return ResponseEntity.ok(Map.of(
            "message", "Seeded " + count + " demo trends",
            "timestamp", Instant.now().toString()
        ));
    }

}
