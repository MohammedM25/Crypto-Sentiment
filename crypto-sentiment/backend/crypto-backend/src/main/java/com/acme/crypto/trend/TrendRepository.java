package com.acme.crypto.trend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface TrendRepository extends JpaRepository<Trend, Long> {
    List<Trend> findByPlatformIgnoreCase(String platform);
    List<Trend> findByTopicIgnoreCase(String topic);
    List<Trend> findByCapturedAtAfter(Instant cutoff);
    
    @Modifying
    @Query("DELETE FROM Trend t WHERE LOWER(t.topic) = LOWER(:topic)")
    void deleteByTopicIgnoreCase(@Param("topic") String topic);
    
}
