package com.acme.crypto.scraper;

import com.acme.crypto.trend.Trend;
import com.acme.crypto.trend.TrendRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;

/**
 * Scrapes crypto news from RSS feeds and saves sentiment to the database.
 * Runs every 2 hours. No API keys required.
 */
@Service
@ConditionalOnProperty(name = "scraper.enabled", havingValue = "true", matchIfMissing = false)
public class NewsScraperService {

    private static final Logger log = LoggerFactory.getLogger(NewsScraperService.class);

    private static final String[] RSS_FEEDS = {
        "https://www.coindesk.com/arc/outboundfeeds/rss/",
        "https://cointelegraph.com/rss",
        "https://cryptonews.com/news/feed/"
    };

    private static final Map<String, String> CRYPTO_MAP = Map.ofEntries(
        Map.entry("bitcoin", "BTC"), Map.entry("btc", "BTC"),
        Map.entry("ethereum", "ETH"), Map.entry("eth", "ETH"),
        Map.entry("solana", "SOL"), Map.entry("sol", "SOL"),
        Map.entry("cardano", "ADA"), Map.entry("ada", "ADA"),
        Map.entry("ripple", "XRP"), Map.entry("xrp", "XRP"),
        Map.entry("dogecoin", "DOGE"), Map.entry("doge", "DOGE"),
        Map.entry("polygon", "MATIC"), Map.entry("matic", "MATIC"),
        Map.entry("avalanche", "AVAX"), Map.entry("avax", "AVAX"),
        Map.entry("chainlink", "LINK"), Map.entry("link", "LINK"),
        Map.entry("polkadot", "DOT"), Map.entry("dot", "DOT"),
        Map.entry("litecoin", "LTC"), Map.entry("ltc", "LTC"),
        Map.entry("uniswap", "UNI"), Map.entry("uni", "UNI"),
        Map.entry("shiba", "SHIB"), Map.entry("shib", "SHIB"),
        Map.entry("tron", "TRX"), Map.entry("trx", "TRX"),
        Map.entry("cosmos", "ATOM"), Map.entry("atom", "ATOM"),
        Map.entry("near", "NEAR"), Map.entry("algorand", "ALGO"),
        Map.entry("algo", "ALGO"), Map.entry("tezos", "XTZ"),
        Map.entry("xtz", "XTZ"), Map.entry("filecoin", "FIL"),
        Map.entry("fil", "FIL"), Map.entry("aptos", "APT"),
        Map.entry("apt", "APT")
    );

    private static final Set<String> POSITIVE = Set.of(
        "surge", "rally", "gain", "growth", "bullish", "breakthrough",
        "adoption", "partnership", "launch", "upgrade", "success",
        "moon", "pump", "hodl", "buy", "invest", "opportunity"
    );

    private static final Set<String> NEGATIVE = Set.of(
        "crash", "drop", "fall", "bearish", "decline", "loss",
        "hack", "scam", "rug", "ban", "regulation", "sell",
        "dump", "fear", "risk", "warning", "concern"
    );

    @Autowired
    private TrendRepository trendRepository;

    @Value("${scraper.max-items-per-run:20}")
    private int maxItemsPerRun;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build();

    @EventListener(ApplicationReadyEvent.class)
    public void scrapeOnStartup() {
        log.info("Running initial scrape on startup");
        scrape();
    }

    @Scheduled(fixedRateString = "${scraper.interval-ms:7200000}") // 2 hours default
    public void scrape() {
        log.info("Starting news scrape from RSS feeds");
        int saved = 0;
        Set<String> seen = new HashSet<>();

        for (String feedUrl : RSS_FEEDS) {
            try {
                List<ScrapedItem> items = fetchRss(feedUrl);
                for (ScrapedItem item : items) {
                    if (saved >= maxItemsPerRun) break;
                    String key = item.title + "|" + item.source;
                    if (seen.contains(key)) continue;
                    seen.add(key);

                    String crypto = extractCrypto(item.title + " " + item.description);
                    if (crypto == null) crypto = "GENERAL";

                    double score = sentimentScore(item.title + " " + item.description);

                    Trend t = new Trend();
                    t.setTopic(crypto);
                    t.setPlatform(item.source);
                    t.setScore(score);
                    t.setCapturedAt(Instant.now());
                    trendRepository.save(t);
                    saved++;
                }
            } catch (Exception e) {
                log.warn("Failed to fetch {}: {}", feedUrl, e.getMessage());
            }
        }
        log.info("Scrape complete: saved {} new trends", saved);
    }

    private List<ScrapedItem> fetchRss(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url))
            .header("User-Agent", "CryptoSentiment/1.0")
            .timeout(java.time.Duration.ofSeconds(15))
            .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) return List.of();

        Document doc = Jsoup.parse(res.body(), "", org.jsoup.parser.Parser.xmlParser());
        List<ScrapedItem> items = new ArrayList<>();
        String source = url.contains("coindesk") ? "coindesk" : url.contains("cointelegraph") ? "cointelegraph" : "cryptonews";

        Elements entries = doc.select("item");
        if (entries.isEmpty()) entries = doc.select("entry");

        for (Element e : entries) {
            String title = text(e, "title");
            String desc = text(e, "description");
            if (title == null || title.isBlank()) continue;
            items.add(new ScrapedItem(title, desc != null ? desc : "", source));
        }
        return items;
    }

    private String text(Element parent, String tag) {
        Element el = parent.selectFirst(tag);
        return el != null ? el.text().trim() : null;
    }

    private String extractCrypto(String text) {
        String lower = text.toLowerCase();
        for (Map.Entry<String, String> e : CRYPTO_MAP.entrySet()) {
            if (lower.contains(e.getKey())) return e.getValue();
        }
        return null;
    }

    private double sentimentScore(String text) {
        String lower = text.toLowerCase();
        int pos = 0, neg = 0;
        for (String w : POSITIVE) if (lower.contains(w)) pos++;
        for (String w : NEGATIVE) if (lower.contains(w)) neg++;
        int total = pos + neg;
        if (total == 0) return 0.0;
        return (pos - neg) / (double) total;
    }

    private record ScrapedItem(String title, String description, String source) {}
}
