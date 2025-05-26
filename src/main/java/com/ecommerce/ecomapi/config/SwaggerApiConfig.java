package com.ecommerce.ecomapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

//@OpenAPIDefinition
@Configuration
@OpenAPIDefinition(
      info = @Info(title = "E-commerce API", version = "1.0"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerApiConfig {
//    @Bean
//    public OpenAPI baseOpenAPI() {
//        return new OpenAPI().info(new Info().title("Dashboard API").version("1.0.0").description("Dashboard API"));
//    }
}
