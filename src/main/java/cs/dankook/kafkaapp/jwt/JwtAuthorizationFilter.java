package cs.dankook.kafkaapp.jwt;

import cs.dankook.kafkaapp.kafka.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthorizationFilter implements GatewayFilter {

    private final JwtUtil jwtUtil;
    private final KafkaProducer kafkaProducer;

    @Autowired
    public JwtAuthorizationFilter(JwtUtil jwtUtil, KafkaProducer kafkaProducer) {
        this.jwtUtil = jwtUtil;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7); // "Bearer " 제거

        try {
            // JWT 검증
            SecretKey key = Keys.hmacShaKeyFor(jwtUtil.getSecretKey().getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 유저 정보 추출
            String userEmail = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            // ADMIN 검증 (ADMIN 경로에만 필요)
            if (exchange.getRequest().getPath().toString().startsWith("/admin")) {
                if (!"ADMIN".equals(role)) {
                    return onError(exchange, "Forbidden: Admin access only", HttpStatus.FORBIDDEN);
                }
            }

            // Kafka 메시지 생성 및 전송
            String topic = "auth-logs"; // 예: 고정된 토픽 이름 (동적 토픽도 가능)
            String message = String.format("{\"userEmail\":\"%s\", \"role\":\"%s\", \"path\":\"%s\"}",
                    userEmail, role, exchange.getRequest().getPath().toString());
            kafkaProducer.sendMessage(topic, message);

            // 유저 정보를 요청 헤더에 추가
            exchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-User-Email", userEmail)
                            .header("X-User-Role", role)
                            .build())
                    .build();

            return chain.filter(exchange);

        } catch (Exception e) {
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(err.getBytes(StandardCharsets.UTF_8))));
    }
}