package br.com.fiap.videosapi.video.infrastructure.kafka;

import br.com.fiap.videosapi.video.application.usecase.VideoStatusUpdateUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.event.VideoStatusUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "kafka.mock.enabled", havingValue = "true")
public class MockVideoStatusUpdateConsumer implements MessageListener {

    private final VideoStatusUpdateUseCase videoStatusUpdateUseCase;
    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private static final String VIDEO_STATUS_UPDATE_CHANNEL = "video-status-update-events";

    @PostConstruct
    public void subscribeToStatusUpdates() {
        log.info("Subscribing to Redis channel for video status updates: {}", VIDEO_STATUS_UPDATE_CHANNEL);
        redisMessageListenerContainer.addMessageListener(this, new ChannelTopic(VIDEO_STATUS_UPDATE_CHANNEL));
    }

    @Override
    public void onMessage(@org.springframework.lang.NonNull Message message, @org.springframework.lang.Nullable byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            log.info("Received video status update message from Redis channel: {}", messageBody);

            VideoStatusUpdateEvent event = objectMapper.readValue(messageBody, VideoStatusUpdateEvent.class);
            log.info("Processing status update for video ID: {} to status: {}",
                    event.getVideoId(), event.getNewStatus());

            videoStatusUpdateUseCase.processStatusUpdateEvent(event);

            log.info("Successfully processed status update for video ID: {}", event.getVideoId());

        } catch (Exception e) {
            log.error("Error processing video status update message: {}", new String(message.getBody()), e);
        }
    }
}
