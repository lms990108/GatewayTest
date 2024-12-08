package cs.dankook.kafkaapp.kafka;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 동적으로 Kafka 메시지를 특정 토픽으로 전송
     *
     * @param topic   Kafka 토픽 이름
     * @param message 메시지 내용
     */
    public void sendMessage(String topic, String message) {
        // KafkaTemplate.send() 메서드는 비동기 방식으로 작동하고 CompletableFuture를 반환
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);

        // whenComplete 사용하여 성공 또는 실패 시 후속 작업 처리
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                // 실패한 경우
                logger.error("Error sending message: \"{}\" to topic: {}", message, topic, ex);
            } else {
                // 성공한 경우
                logger.info("Message sent successfully: \"{}\" to topic: {}", message, topic);
                logger.info("Metadata: {}", result.getRecordMetadata());
            }
        });
    }
}