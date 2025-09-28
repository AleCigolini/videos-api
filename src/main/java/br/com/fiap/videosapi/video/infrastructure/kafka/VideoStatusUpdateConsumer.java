package br.com.fiap.videosapi.video.infrastructure.kafka;

import br.com.fiap.videosapi.video.application.usecase.VideoStatusUpdateUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.event.VideoStatusUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoStatusUpdateConsumer {

    private final VideoStatusUpdateUseCase videoStatusUpdateUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${topics.video-status-update}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeVideoStatusUpdate(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {

        log.info("Received video status update message from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);

        try {
            VideoStatusUpdateEvent event = objectMapper.readValue(message, VideoStatusUpdateEvent.class);
            log.info("Processing status update for video ID: {} to status: {} (userId={})",
                    event.getVideoId(), event.getStatus(), event.getUserId());

            if (event.getUserId() == null || event.getUserId().isBlank()) {
                throw new IllegalArgumentException("Missing userId in VideoStatusUpdateEvent");
            }

            videoStatusUpdateUseCase.processStatusUpdateEvent(event);

            log.info("Successfully processed status update for video ID: {}", event.getVideoId());

        } catch (Exception e) {
            log.error("Error processing video status update message: {}", message, e);
            throw new RuntimeException("Failed to process video status update", e);
        }
    }
}
