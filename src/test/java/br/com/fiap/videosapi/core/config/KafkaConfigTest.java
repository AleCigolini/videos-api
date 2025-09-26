package br.com.fiap.videosapi.core.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = KafkaConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.consumer.group-id=test-group"
})
class KafkaConfigTest {

    @Autowired
    private KafkaConfig kafkaConfig;

    @Test
    @DisplayName("Deve criar ProducerFactory com propriedades corretas")
    void deveCriarProducerFactoryComPropriedadesCorretas() {
        ProducerFactory<String, String> factory = kafkaConfig.producerFactory();
        Map<String, Object> config = factory.getConfigurationProperties();
        assertThat(config.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(config.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(config.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(config.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(config.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(3);
        assertThat(config.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);
        assertThat(config.get(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve criar KafkaTemplate corretamente")
    void deveCriarKafkaTemplateCorretamente() {
        KafkaTemplate<String, String> template = kafkaConfig.kafkaTemplate();
        assertThat(template).isNotNull();
        assertThat(template.getProducerFactory()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar ConsumerFactory com propriedades corretas")
    void deveCriarConsumerFactoryComPropriedadesCorretas() {
        ConsumerFactory<String, String> factory = kafkaConfig.consumerFactory();
        Map<String, Object> config = factory.getConfigurationProperties();
        assertThat(config.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(config.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo("test-group");
        assertThat(config.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)).isEqualTo(StringDeserializer.class);
        assertThat(config.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)).isEqualTo(StringDeserializer.class);
        assertThat(config.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)).isEqualTo("earliest");
        assertThat(config.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)).isEqualTo(false);
        assertThat(config.get(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG)).isEqualTo(30000);
        assertThat(config.get(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG)).isEqualTo(10000);
    }

    @Test
    @DisplayName("Deve criar ConcurrentKafkaListenerContainerFactory com propriedades corretas")
    void deveCriarKafkaListenerContainerFactoryComPropriedadesCorretas() throws Exception {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = kafkaConfig.kafkaListenerContainerFactory();
        assertThat(factory).isNotNull();
        assertThat(factory.getConsumerFactory()).isNotNull();
        java.lang.reflect.Field concurrencyField = ConcurrentKafkaListenerContainerFactory.class.getDeclaredField("concurrency");
        concurrencyField.setAccessible(true);
        int concurrency = (int) concurrencyField.get(factory);
        assertThat(concurrency).isEqualTo(3);
        assertThat(factory.getContainerProperties().getAckMode().name()).isEqualTo("MANUAL_IMMEDIATE");
        assertThat(factory.getContainerProperties().isSyncCommits()).isTrue();
    }
}
