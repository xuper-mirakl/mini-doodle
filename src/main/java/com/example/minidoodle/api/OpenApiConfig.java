package com.example.minidoodle.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI api() {
    return new OpenAPI().info(new Info()
        .title("mini-doodle API")
        .version("1.0.0")
        .description("Mini Doodle: users manage slots, book meetings, query availability."));
  }
}
