package br.com.fiap.videosapi.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
@Profile("local")
public class DevelopmentConfig {

//    @Bean
//    @Primary
//    @ConditionalOnProperty(name = "azure.storage.mock.enabled", havingValue = "true")
//    public AzureBlobStorageService mockAzureBlobStorageService() {
//        log.info("Initializing Mock Azure Blob Storage Service for development");
//        return new MockAzureBlobStorageService();
//    }
//
//    @Bean
//    @Primary
//    @ConditionalOnProperty(name = "kafka.mock.enabled", havingValue = "true")
//    public VideoEventPublisher mockVideoEventProducer(ObjectMapper objectMapper,
//                                                      RedisTemplate<String, String> redisTemplate) {
//        log.info("Initializing Mock Kafka Video Event Producer for development");
//        return new MockVideoEventProducer(objectMapper, redisTemplate);
//    }
}
