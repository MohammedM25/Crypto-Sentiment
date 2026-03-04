package com.acme.crypto.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Configures DataSource from DATABASE_URL (Render/Heroku format) when present.
 * Format: postgres://user:password@host:port/database
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource dataSourceFromUrl(@Value("${DATABASE_URL}") String databaseUrl) {
        try {
            // postgres://user:password@host:port/database -> jdbc:postgresql://host:port/database
            URI uri = new URI(databaseUrl.replace("postgres://", "postgresql://"));
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            String database = path != null && path.length() > 1 ? path.substring(1) : "postgres";
            String userInfo = uri.getUserInfo();
            String user = "";
            String password = "";
            if (userInfo != null) {
                int colonIdx = userInfo.indexOf(':');
                user = colonIdx >= 0 ? userInfo.substring(0, colonIdx) : userInfo;
                password = colonIdx >= 0 ? userInfo.substring(colonIdx + 1) : "";
            }

            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require");
            ds.setUsername(user);
            ds.setPassword(password);
            ds.setMaximumPoolSize(5);
            ds.setMinimumIdle(1);
            return ds;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse DATABASE_URL", e);
        }
    }
}
