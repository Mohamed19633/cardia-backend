package com.java.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient predictionWebClient() {
        return WebClient.builder().baseUrl("http://localhost:5000").build();
    }

}
