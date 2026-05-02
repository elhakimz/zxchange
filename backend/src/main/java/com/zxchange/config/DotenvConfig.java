package com.zxchange.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.nio.file.Paths;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public void init() {
        // Load .env from the root directory (one level up from backend)
        Dotenv dotenv = Dotenv.configure()
                .directory("..") 
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }
}
