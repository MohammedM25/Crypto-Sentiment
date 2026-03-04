package com.acme.crypto.scraper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "scraper.enabled", havingValue = "true", matchIfMissing = false)
public class ScraperController {

    private final NewsScraperService scraperService;

    public ScraperController(NewsScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @PostMapping("/scrape")
    @GetMapping("/scrape")
    public ResponseEntity<Map<String, Object>> triggerScrape() {
        new Thread(() -> scraperService.scrape()).start();
        return ResponseEntity.ok(Map.of(
            "message", "Scrape started in background. Check /api/trends in a minute.",
            "timestamp", Instant.now().toString()
        ));
    }
}
