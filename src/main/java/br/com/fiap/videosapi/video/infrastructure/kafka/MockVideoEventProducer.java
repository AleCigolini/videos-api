package br.com.fiap.videosapi.video.infrastructure.kafka;

import br.com.fiap.videosapi.video.common.domain.dto.event.VideoUploadEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@ConditionalOnProperty(name = "kafka.mock.enabled", havingValue = "true")
public class MockVideoEventProducer implements VideoEventPublisher {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public MockVideoEventProducer(ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void publishVideoUploadEvent(VideoUploadEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = "mock:video-upload-events:" + event.getVideoId();
            
            // Store event in Redis with TTL for mock purposes
            redisTemplate.opsForValue().set(key, eventJson, 1, TimeUnit.HOURS);
            
            // Also store in a list for easy retrieval
            String listKey = "mock:video-upload-events:list";
            redisTemplate.opsForList().leftPush(listKey, eventJson);
            redisTemplate.expire(listKey, 1, TimeUnit.HOURS);
            
            log.info("Successfully published mock video upload event for videoId: {} at {}. Event stored in Redis with key: {}", 
                    event.getVideoId(), LocalDateTime.now(), key);
                    
        } catch (JsonProcessingException e) {
            log.error("Error serializing mock video upload event for videoId: {}", event.getVideoId(), e);
        }
    }
}
