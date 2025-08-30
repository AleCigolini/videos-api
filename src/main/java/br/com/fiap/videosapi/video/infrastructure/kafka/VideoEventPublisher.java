package br.com.fiap.videosapi.video.infrastructure.kafka;

import br.com.fiap.videosapi.video.common.domain.dto.event.VideoUploadEvent;

public interface VideoEventPublisher {
    void publishVideoUploadEvent(VideoUploadEvent event);
}
