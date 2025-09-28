package br.com.fiap.videosapi.video.infrastructure.kafka;

import br.com.fiap.videosapi.video.common.domain.dto.event.VideoUploadEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoEventProducer implements VideoEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${topics.video-upload}")
    private String videoUploadTopic;

    public void publishVideoUploadEvent(VideoUploadEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = String.valueOf(event.getVideoId());
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(videoUploadTopic, key, eventJson);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Successfully published video upload event for videoId: {} to topic: {} with offset: {}",
                            event.getVideoId(), videoUploadTopic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish video upload event for videoId: {} to topic: {}",
                            event.getVideoId(), videoUploadTopic, exception);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("Error serializing video upload event for videoId: {}", event.getVideoId(), e);
        }
    }
}
