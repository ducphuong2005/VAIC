package com.careercompass;

import com.careercompass.config.CorsProperties;
import com.careercompass.config.InternalApiProperties;
import com.careercompass.config.JwtProperties;
import com.careercompass.config.LlmProperties;
import com.careercompass.config.RecommendationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class, InternalApiProperties.class, RecommendationProperties.class, LlmProperties.class})
public class CareerCompassApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareerCompassApplication.class, args);
    }
}
