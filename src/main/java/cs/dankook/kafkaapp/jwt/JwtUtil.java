package cs.dankook.kafkaapp.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    // 시크릿 키는 application.properties나 환경 변수에서 가져옵니다.
    @Value("${jwt.secret}")
    private String secretKey;

    // JWT 생성
    public String generateToken(String userEmail, String role) {
        long expirationTime = 1000 * 60 * 60; // 1시간 (예: 만료 시간 설정)

        return Jwts.builder()
                .setSubject(userEmail)  // 사용자 이메일을 subject로 설정
                .claim("role", role)    // 추가적인 정보(예: 역할)를 claim에 설정
                .setIssuedAt(new Date())  // 생성 일시 설정
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))  // 만료 시간 설정
                .signWith(SignatureAlgorithm.HS256, secretKey)  // 서명
                .compact();
    }

    // JWT 해석
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // JWT 토큰에서 사용자 이메일 추출
    public String getUserEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // JWT 토큰에서 역할(role) 추출
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // JWT 유효성 검사 (토큰이 만료되었는지 확인)
    public boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    // 토큰 검증 메서드 (필요시 사용할 수 있음)
    public boolean validateToken(String token, String userEmail) {
        return (userEmail.equals(getUserEmail(token)) && !isTokenExpired(token));
    }
}
