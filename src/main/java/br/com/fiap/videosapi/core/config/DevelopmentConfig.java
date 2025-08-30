package br.com.fiap.videosapi.core.config;

import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobStorageService;
import br.com.fiap.videosapi.video.infrastructure.azure.MockAzureBlobStorageService;
import br.com.fiap.videosapi.video.infrastructure.kafka.MockVideoEventProducer;
import br.com.fiap.videosapi.video.infrastructure.kafka.VideoEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Slf4j
@Profile("local")
public class DevelopmentConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "azure.storage.mock.enabled", havingValue = "true")
    public AzureBlobStorageService mockAzureBlobStorageService() {
        log.info("Initializing Mock Azure Blob Storage Service for development");
        return new MockAzureBlobStorageService();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "kafka.mock.enabled", havingValue = "true")
    public VideoEventPublisher mockVideoEventProducer(ObjectMapper objectMapper, 
                                                      RedisTemplate<String, String> redisTemplate) {
        log.info("Initializing Mock Kafka Video Event Producer for development");
        return new MockVideoEventProducer(objectMapper, redisTemplate);
    }
}
