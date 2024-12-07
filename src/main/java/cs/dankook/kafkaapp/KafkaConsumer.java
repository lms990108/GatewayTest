package cs.dankook.kafkaapp;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "test-topic", groupId = "group_id")
    public void consumeMessage(String message) {
        System.out.println("Consumed message: " + message);
    }
}
