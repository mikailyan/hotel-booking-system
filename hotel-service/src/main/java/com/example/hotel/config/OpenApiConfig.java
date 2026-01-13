package com.example.hotel.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI openAPI() {
    var scheme = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");
    return new OpenAPI()
        .info(new Info().title("Hotel Service API").version("1.0.0"))
        .components(new Components().addSecuritySchemes("bearerAuth", scheme))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}
