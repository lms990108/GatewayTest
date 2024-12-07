package cs.dankook.kafkaapp.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 동적으로 Kafka 메시지를 특정 토픽으로 전송
     *
     * @param topic   Kafka 토픽 이름
     * @param message 메시지 내용
     */
    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Produced message: \"" + message + "\" to topic: " + topic);
    }
}
