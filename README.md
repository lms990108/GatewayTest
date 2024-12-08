# Gateway Project with JWT and Kafka

## 프로젝트 개요

- 이 프로젝트는 Spring Cloud Gateway를 활용하여 요청 라우팅을 수행하며, 보안 강화를 위해 JWT(JSON Web Token)를 검증하고 새로운 JWT를 생성하여 Kafka를 통해 전송하는 기능을 포함하고 있습니다. 게이트웨이는 단순한 요청 라우팅뿐만 아니라, 인증 및 인가 로직을 수행하고 내부 서비스와의 통신에 필요한 JWT를 동적으로 생성하여 Kafka 메시지로 전달합니다.
- 내부 JWT 의 필요성에 대한 아이디어 출처 : https://toss.tech/article/slash23-server
---

## 주요 기능

1. **JWT 인증 및 인가**
    - 클라이언트로부터 전달받은 JWT를 검증하여 사용자 정보를 추출.
    - 사용자 정보 및 권한(Role)을 기반으로 내부 서비스를 위한 새로운 JWT 생성.
2. **Spring Cloud Gateway 라우팅**
    - `/api/**` 경로로 들어오는 요청을 내부 애플리케이션으로 전달.
    - 요청 경로를 리라이트하여 내부 서비스와의 통합을 용이하게 구성.
3. **Kafka 연동**
    - 새로운 JWT와 요청 정보를 Kafka 메시지로 생성 및 전송.
    - 메시지는 `auth-logs`와 같은 지정된 토픽으로 전송.
4. **JUnit 기반 테스트**
    - JWT 검증 및 파싱 테스트.
    - 게이트웨이 라우팅 테스트.
    - Kafka 메시지 전송 성공 여부 테스트.

---

## 기술 스택

- **Backend**: Spring Boot, Spring Cloud Gateway
- **Messaging**: Apache Kafka
- **JWT**: io.jsonwebtoken (JJWT 라이브러리)
- **Testing**: JUnit 5, Mockito
- **Logging**: SLF4J, Logback

---

## 설치 및 실행 방법

### 1. 요구 사항

- Java 17 이상
- Gradle 7.5 이상
- Kafka 및 Zookeeper 실행 환경
- Docker (옵션)

### 2. 프로젝트 실행

1. **Kafka 및 Zookeeper 설정**

    - Kafka와 Zookeeper를 실행합니다. Docker Compose를 사용하는 경우:
      ```bash
      docker-compose up -d
      ```

2. **Spring Boot 애플리케이션 실행**

    - 프로젝트를 빌드하고 실행합니다:
      ```bash
      ./gradlew clean build -x test
      ./gradlew bootRun
      ```
    - `./gradlew clean build`를 실행하면 기본적으로 모든 테스트가 실행됩니다. 그러나 Kafka 관련 테스트에서 네트워크 환경에 따라 실패할 수 있어, `-x test` 플래그를 사용하여 테스트를 생략한 빌드를 진행할 수 있습니다.
    - 테스트를 생략하지 않고 빌드를 진행하려면 Kafka 및 Zookeeper가 반드시 실행된 상태여야 합니다. 인텔리제이 또는 별도의 IDE에서 수동으로 실행하는 경우 이러한 제약이 발생하지 않습니다.

3. **환경 변수 설정**

    - `application.properties` 또는 `application.yml`에서 Kafka 브로커와 관련된 설정을 확인 및 수정합니다.
      ```properties
      spring.kafka.bootstrap-servers=localhost:9092
      test.api.path=/api/**
      ```

---

## 테스트 실행

### 1. 테스트 방법

1. **JUnit 테스트 실행**

    - Gradle을 통해 테스트 실행:
      ```bash
      ./gradlew test
      ```

2. **테스트 내용**

    - **JWT 검증 및 파싱**: JWT 생성 및 파싱 결과가 기대 값과 일치하는지 확인.
    - **게이트웨이 라우팅**: `/api/**` 경로가 정상적으로 내부 서비스로 전달되는지 확인.
    - **Kafka 메시지 전송**: JWT와 요청 정보가 지정된 Kafka 토픽으로 성공적으로 전송되었는지 확인.

### 2. 테스트 예제

- `JwtRouteTest` 클래스 내 주요 테스트:
    - **JWT 라우팅 테스트**: `testJwtRouting()`
    - **JWT 파싱 테스트**: `testJwtParsing()`
    - **Kafka 메시지 전송 테스트**: `testKafkaMessageSent()`

---

## 디렉토리 구조

```
cs.dankook.kafkaapp
├── config
│   └── GatewayConfig.java  # Gateway 라우팅 설정
├── controller
│   └── MockController.java  # Mock 컨트롤러
├── jwt
│   ├── JwtAuthorizationFilter.java  # JWT 인증 및 인가 필터
│   └── JwtUtil.java  # JWT 생성 및 파싱 유틸리티
├── kafka
│   └── KafkaProducer.java  # Kafka 메시지 프로듀서
├── test
│   └── JwtRouteTest.java  # JUnit 테스트 클래스
```

---

## 추가 정보

- **Kafka 테스트 실패 문제**
    - Gradle 빌드 시 Kafka 관련 테스트(`testKafkaMessageSent`)가 실행되며, Kafka 브로커와의 연결 실패로 인해 빌드가 중단될 수 있습니다.
    - 이러한 문제를 방지하기 위해 다음과 같은 방법을 사용할 수 있습니다:
        1. Kafka 및 Zookeeper를 로컬 환경에서 미리 실행한 후 테스트를 진행.
        2. `./gradlew clean build -x test` 명령을 사용하여 테스트를 제외한 빌드를 실행.
        3. 테스트용 Kafka 환경을 Docker Compose로 구성하여 의존성을 해결.

- **로그 확인**
    - Kafka 메시지 전송 및 오류 발생 시 Logback을 통해 상세 로그 확인 가능.

- **Kafka 토픽**
    - 기본 설정: `auth-logs` 토픽 사용.
    - 필요 시 Kafka 설정 파일에서 토픽 이름 변경 가능.

- **Docker Compose 환경 미흡 사항**
    - Kafka 및 Zookeeper와 `kafka-app`을 Docker Compose로 연동하여 전체 환경을 구성하려 했으나, 네트워크 관련 이슈로 인해 현재 완전한 환경을 제공하지 못하고 있습니다.
    - 이 부분은 향후 개선 계획에 포함되어 있으며, 네트워크 설정 문제를 해결하는 데 중점을 두고 있습니다.

