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
        .path("/api/user/**")
        .uri("http://user-service:8082"))
      .route("review-route", r -> r
        .path("/api/review/**")
        .uri("http://review-service:8083"))
      .route("book-route", r -> r
        .path("/api/book/**")
        .uri("http://book-service:8084"))
      .route("stats-route", r -> r
        .path("/api/stats/**")
        .uri("http://admin-service:8085"))
      .route("admin-route", r -> r
        .path("/api/admin/**")
        .uri("http://admin-service:8085"))
      .route("notification-route", r -> r
        .path("/api/notification/**")
        .uri("http://notification-service:8087"))
      .build();
  }
}
