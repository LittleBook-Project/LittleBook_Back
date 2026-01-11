package com.littlebook.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@SpringBootApplication
public class GatewayApplication {
  
  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, args);
  }

  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.addAllowedOrigin("http://localhost:5173");
    corsConfig.addAllowedOrigin("http://localhost:3000");
    corsConfig.addAllowedMethod("*");
    corsConfig.addAllowedHeader("*");
    corsConfig.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);
    return new CorsWebFilter(source);
  }

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
      // Auth service - keep /api prefix
      .route("auth-route", r -> r
        .path("/api/auth/**")
        .filters(f -> f.rewritePath("/api/auth(?<segment>/.*)?", "/api${segment}"))
        .uri("http://auth-service:8081"))
      
      // User service - rewrite /api/user(s)? to /user
      .route("user-route", r -> r
        .path("/api/user/**", "/api/users/**")
        .filters(f -> f.rewritePath("/api/users?(?<segment>/.*)?", "/user${segment}"))
        .uri("http://user-service:8082"))
      
      // Review service - rewrite /api/review(s)? to /review
      .route("review-route", r -> r
        .path("/api/review/**", "/api/reviews/**")
        .filters(f -> f.rewritePath("/api/reviews?(?<segment>/.*)?", "/review${segment}"))
        .uri("http://review-service:8083"))
      
      // Book service - rewrite /api/books? to /books
      .route("book-route", r -> r
        .path("/api/book", "/api/books", "/api/book/**", "/api/books/**")
        .filters(f -> f.rewritePath("/api/books?(?<segment>/.*)?", "/books${segment}"))
        .uri("http://book-service:8084"))
      
      // Admin service - keep /api/stats prefix
      .route("stats-route", r -> r
        .path("/api/stats/**")
        .uri("http://admin-service:8085"))
      
      // Admin service - other admin endpoints
      .route("admin-route", r -> r
        .path("/api/admin/**")
        .uri("http://admin-service:8085"))
      
      .build();
  }
}
