package br.com.fiap.videosapi.video.infrastructure.kafka;

import br.com.fiap.videosapi.video.application.usecase.VideoStatusUpdateUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.event.VideoStatusUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "kafka.mock.enabled", havingValue = "false", matchIfMissing = true)
public class VideoStatusUpdateConsumer {

    private final VideoStatusUpdateUseCase videoStatusUpdateUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.video-status-update:video-status-update-events}",
            groupId = "${kafka.consumer.group-id:video-api-consumer-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeVideoStatusUpdate(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received video status update message from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            VideoStatusUpdateEvent event = objectMapper.readValue(message, VideoStatusUpdateEvent.class);
            log.info("Processing status update for video ID: {} to status: {}", 
                    event.getVideoId(), event.getNewStatus());
            
            videoStatusUpdateUseCase.processStatusUpdateEvent(event);
            
            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();
            
            log.info("Successfully processed status update for video ID: {}", event.getVideoId());
            
        } catch (Exception e) {
            log.error("Error processing video status update message: {}", message, e);
            // Don't acknowledge on error - message will be retried
            throw new RuntimeException("Failed to process video status update", e);
        }
    }
}
