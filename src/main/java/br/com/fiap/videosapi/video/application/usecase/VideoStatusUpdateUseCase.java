package br.com.fiap.videosapi.video.application.usecase;

import br.com.fiap.videosapi.video.common.domain.dto.event.VideoStatusUpdateEvent;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;

public interface VideoStatusUpdateUseCase {
    void updateVideoStatus(Long videoId, String status);
    void processStatusUpdateEvent(VideoStatusUpdateEvent event);
}
