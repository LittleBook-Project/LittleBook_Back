package com.littlebook.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {
  
  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, args);
  }

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
      .route("auth-route", r -> r
        .path("/api/auth/**")
        .uri("http://auth-service:8081"))
      .route("user-route", r -> r
        .path("/api/users/**")
        .uri("http://user-service:8082"))
      .route("review-route", r -> r
        .path("/api/reviews/**")
        .uri("http://review-service:8083"))
      .route("book-route", r -> r
        .path("/api/books/**")
        .uri("http://book-service:8084"))
      .route("admin-route", r -> r
        .path("/api/admin/**")
        .uri("http://admin-service:8085"))
      .build();
  }
}
