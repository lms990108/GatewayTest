package cs.dankook.kafkaapp.config;

import cs.dankook.kafkaapp.jwt.JwtAuthorizationFilter;
import lombok.Getter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Configuration
public class GatewayConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    public GatewayConfig(JwtAuthorizationFilter jwtAuthorizationFilter) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    @Value("${api.url}")
    private String apiUrl;

    /**
     * API 라우팅
     * /api/**로 들어오는 요청을 8080포트 application 으로 전달.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtAuthorizationFilter jwtAuthorizationFilter) {
        System.out.printf("apiUrl: %s%n", apiUrl);
        return builder.routes()
                // /api/** 경로
                .route("api-route", r -> r.path("/api/**")
                        .filters(f -> f.filter(jwtAuthorizationFilter) // JWT 필터
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")) // Path Rewrite 필터
                        .uri(apiUrl))
                .build();
    }

}