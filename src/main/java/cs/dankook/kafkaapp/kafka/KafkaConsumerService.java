package cs.dankook.kafkaapp.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaConsumerService {

    private KafkaConsumer<String, String> consumer;

    public KafkaConsumerService() {
        // Kafka Consumer 설정
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumer = new KafkaConsumer<>(consumerProps);
    }

    // Kafka 메시지를 소비하는 메서드
    public void consumeMessages(String topic) {
        consumer.subscribe(java.util.Collections.singletonList(topic));

        // 메시지 소비
        while (true) {
            var records = consumer.poll(1000); // 1초 대기
            records.forEach(record -> {
                // 여기서 메시지를 처리하는 로직 작성
                System.out.println("Consumed message: " + record.value());
                // 예시로 받은 메시지가 "newJwt"를 포함하는지 확인
                if (record.value().contains("newJwt")) {
                    System.out.println("Valid message received: " + record.value());
                }
            });
        }
    }

    // Consumer 종료
    public void close() {
        if (consumer != null) {
            consumer.close();
        }
    }
}
