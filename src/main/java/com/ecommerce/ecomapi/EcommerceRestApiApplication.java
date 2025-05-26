package com.ecommerce.ecomapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@Validated
public class EcommerceRestApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(EcommerceRestApiApplication.class, args);
	}

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
