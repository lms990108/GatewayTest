package cs.dankook.kafkaapp;

import cs.dankook.kafkaapp.jwt.JwtAuthorizationFilter;
import cs.dankook.kafkaapp.jwt.JwtUtil;
import cs.dankook.kafkaapp.kafka.KafkaProducer;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KafkaAppApplication.class)
public class JwtRouteTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private JwtAuthorizationFilter jwtAuthorizationFilter;


    // 1. 테스트용 이메일과 역할로 JWT 생성
    private String generateTestJwt(String email, String role) {
        // Mock된 JwtUtil에서 반환될 토큰을 정의
        return jwtUtil.generateToken(email, role);
    }

    // 2. JWT를 활용하여 라우팅 테스트
    @Test
    public void testJwtRouting() {
        // 테스트용 이메일과 역할
        String email = "user@example.com";
        String role = "USER";

        // 1. JWT 생성
        String jwt = generateTestJwt(email, role);

        // 2. 생성된 JWT를 사용하여 라우팅 테스트
        webTestClient.get()
                .uri("/api/test")  // /api/test 경로로 요청
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)  // Authorization 헤더에 JWT 추가
                .exchange()
                .expectStatus().isOk()  // 정상적인 응답 상태 확인
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    // 응답 본문에 "Mock Service received path: test"가 포함되어야 함
                    assertTrue(body.contains("Mock Service received path: test"));
                });
    }

    // 3. JWT 해석 테스트 (JWT가 생성되었는지 확인하고, 올바르게 해석되었는지 검증)
    @Test
    public void testJwtParsing() {
        // 테스트용 이메일과 역할
        String email = "user@example.com";
        String role = "USER";

        // 1. JWT 생성
        String jwt = generateTestJwt(email, role);

        // 2. JWT 해석 (JwtUtil에서 제공하는 parseClaims 메서드 사용)
        Claims claims = jwtUtil.parseClaims(jwt);  // Claims 객체 반환
        String parsedEmail = claims.getSubject();  // Subject에서 이메일을 추출
        assertEquals(email, parsedEmail, "Parsed email should match the input email");

        // 3. 생성된 JWT가 만료되지 않았는지 확인
        assertFalse(jwtUtil.isTokenExpired(jwt), "Token should not be expired");
    }

    // 4. Kafka 메시지 전송 확인 테스트
    @Test
    public void testKafkaMessageSent() {
        // 테스트용 이메일과 역할
        String email = "user@example.com";
        String role = "USER";

        // 1. JWT 생성
        String jwt = generateTestJwt(email, role);

        // 2. 생성된 JWT로 카프카 메시지가 전송되는지 확인
        webTestClient.get()
                .uri("/api/test")  // /api/test 경로로 요청
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)  // Authorization 헤더에 JWT 추가
                .exchange()
                .expectStatus().isOk()  // 정상적인 응답 상태 확인
                .expectBody(String.class)
                .consumeWith(response -> {
                    // KafkaProducer가 sendMessage()를 호출했는지 검증
                    verify(kafkaProducer, times(1)).sendMessage(eq("auth-logs"), anyString());
                });
    }

}
