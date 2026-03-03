package com.acme.crypto.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class MarketController {

    // Symbol to CoinGecko ID mapping
    private static final Map<String, String> ID_MAP = new HashMap<String, String>() {{
        put("btc", "bitcoin");
        put("eth", "ethereum");
        put("usdt", "tether");
        put("bnb", "binancecoin");
        put("sol", "solana");
        put("usdc", "usd-coin");
        put("xrp", "ripple");
        put("steth", "staked-ether");
        put("ada", "cardano");
        put("avax", "avalanche-2");
        put("trx", "tron");
        put("wbtc", "wrapped-bitcoin");
        put("link", "chainlink");
        put("dot", "polkadot");
        put("matic", "matic-network");
        put("dai", "dai");
        put("shib", "shiba-inu");
        put("ltc", "litecoin");
        put("bch", "bitcoin-cash");
        put("near", "near");
        put("atom", "cosmos");
        put("uni", "uniswap");
        put("etc", "ethereum-classic");
        put("xlm", "stellar");
        put("icp", "internet-computer");
        put("apt", "aptos");
        put("fil", "filecoin");
        put("hbar", "hedera-hashgraph");
        put("vet", "vechain");
        put("mana", "decentraland");
        put("sand", "the-sandbox");
        put("qnt", "quant-network");
        put("algo", "algorand");
        put("egld", "elrond-erd-2");
        put("flow", "flow");
        put("axs", "axie-infinity");
        put("xtz", "tezos");
        put("aave", "aave");
        put("mkr", "maker");
        put("snx", "havven");
        put("comp", "compound-governance-token");
        put("crv", "curve-dao-token");
        put("1inch", "1inch");
        put("enj", "enjincoin");
        put("bat", "basic-attention-token");
        put("zec", "zcash");
        put("dash", "dash");
        put("nexo", "nexo");
        put("ftm", "fantom");
        put("grt", "the-graph");
    }};

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();
    
    // Cache prices for 2 minutes to reduce API calls
    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MINUTES = 2;

    // Cache top coins for 5 minutes
    private CachedTopCoins topCoinsCache = null;
    private static final long TOP_COINS_CACHE_DURATION_MINUTES = 5;

    @GetMapping("/api/market/price")
    public ResponseEntity<?> price(@RequestParam String symbol) {
        String sym = symbol.toLowerCase();

        // Check cache first
        CachedPrice cached = priceCache.get(sym);
        if (cached != null && !cached.isExpired()) {
            return ResponseEntity.ok(Map.of(
                    "symbol", sym,
                    "usd", cached.price,
                    "source", "cached",
                    "timestamp", cached.timestamp.toString()
            ));
        }

        try {
            String coinId = ID_MAP.get(sym);
            if (coinId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "unsupported symbol",
                        "supported", ID_MAP.keySet()
                ));
            }
            
            String url = String.format("https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd", coinId);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        
            if (response.statusCode() == 200) {
                JsonNode jsonNode = mapper.readTree(response.body());
                JsonNode priceNode = jsonNode.get(coinId).get("usd");
                
                if (priceNode != null) {
                    double price = priceNode.asDouble();
                    priceCache.put(sym, new CachedPrice(price, Instant.now()));
                
                    return ResponseEntity.ok(Map.of(
                            "symbol", sym,
                            "usd", price,
                            "source", "coingecko",
                            "timestamp", Instant.now().toString()
                    ));
                }
            }
            
            return ResponseEntity.status(503).body(Map.of(
                    "error", "External API unavailable",
                    "message", "CoinGecko API is currently unavailable. Please try again later.",
                    "symbol", sym
            ));
            
        } catch (Exception e) {
            System.err.println("Error fetching price for " + sym + ": " + e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                    "error", "External API unavailable",
                    "message", "CoinGecko API is currently unavailable. Please try again later.",
                    "symbol", sym
            ));
        }
    }
        
    
    
    @GetMapping("/api/market/top-coins")
    public ResponseEntity<?> getTopCoins(@RequestParam(defaultValue = "100") int limit) {
        if (topCoinsCache != null && !topCoinsCache.isExpired()) {
            return ResponseEntity.ok(Map.of(
                "timestamp", topCoinsCache.timestamp.toString(),
                "coins", topCoinsCache.coins,
                "source", "cached",
                "message", "Cached data from CoinGecko"
            ));
        }
        
        try {
            String url = String.format("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=%d&page=1&sparkline=false&price_change_percentage=24h", 
                Math.min(limit, 250));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode coinsData = mapper.readTree(response.body());
                topCoinsCache = new CachedTopCoins(coinsData, Instant.now());
                
                return ResponseEntity.ok(Map.of(
                    "timestamp", Instant.now().toString(),
                    "coins", coinsData,
                    "source", "coingecko",
                    "message", "Real-time data from CoinGecko"
                ));
            } else {
                System.err.println("CoinGecko API error: " + response.statusCode() + " - " + response.body());
                return ResponseEntity.status(503).body(Map.of(
                        "error", "External API unavailable",
                        "message", "CoinGecko API is currently unavailable. Please try again later.",
                        "timestamp", Instant.now().toString()
                ));
            }
                
        } catch (Exception e) {
            System.err.println("Error fetching top coins: " + e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                    "error", "External API unavailable",
                    "message", "CoinGecko API is currently unavailable. Please try again later.",
                    "timestamp", Instant.now().toString()
            ));
        }
    }
        
    
    /**
     * Search for cryptocurrencies by name or symbol
     */
    @GetMapping("/api/market/search")
    public ResponseEntity<?> searchCoins(@RequestParam String query) throws Exception {
        try {
            String url = String.format("https://api.coingecko.com/api/v3/search?query=%s", query);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return ResponseEntity.ok(response.body());
            } else {
                return ResponseEntity.status(503).body(Map.of(
                        "error", "External API unavailable",
                        "message", "CoinGecko API is currently unavailable. Please try again later."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "External API unavailable",
                    "message", "CoinGecko API is currently unavailable. Please try again later."
            ));
        }
    }
    
    /**
     * Get detailed information for a specific cryptocurrency
     */
    @GetMapping("/api/market/coin/{id}")
    public ResponseEntity<?> getCoinDetails(@PathVariable String id) throws Exception {
        try {
            String url = String.format("https://api.coingecko.com/api/v3/coins/%s?localization=false&tickers=false&market_data=true&community_data=false&developer_data=false&sparkline=false", id);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return ResponseEntity.ok(response.body());
            } else {
                return ResponseEntity.status(503).body(Map.of(
                        "error", "External API unavailable",
                        "message", "CoinGecko API is currently unavailable. Please try again later."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "External API unavailable",
                    "message", "CoinGecko API is currently unavailable. Please try again later."
            ));
        }
    }

    
    private static class CachedPrice {
        final double price;
        final Instant timestamp;
        
        CachedPrice(double price, Instant timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }
        
        boolean isExpired() {
            return Instant.now().isAfter(timestamp.plus(Duration.ofMinutes(CACHE_DURATION_MINUTES)));
        }
    }
    
    private static class CachedTopCoins {
        final JsonNode coins;
        final Instant timestamp;
        
        CachedTopCoins(JsonNode coins, Instant timestamp) {
            this.coins = coins;
            this.timestamp = timestamp;
        }
        
        boolean isExpired() {
            return Instant.now().isAfter(timestamp.plus(Duration.ofMinutes(TOP_COINS_CACHE_DURATION_MINUTES)));
        }
    }
}