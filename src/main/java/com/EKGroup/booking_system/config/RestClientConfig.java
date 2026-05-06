package com.EKGroup.booking_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @SuppressWarnings("unused")
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
