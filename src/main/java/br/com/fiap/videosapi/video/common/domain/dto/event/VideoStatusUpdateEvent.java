package br.com.fiap.videosapi.video.common.domain.dto.event;

import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoStatusUpdateEvent {

    private Long videoId;
    private String userId;
    private VideoStatus previousStatus;
    private VideoStatus newStatus;
    private String message;
    private LocalDateTime timestamp;
    private String processedBy;

    public static VideoStatusUpdateEvent createStatusUpdateEvent(
            Long videoId,
            VideoStatus previousStatus,
            VideoStatus newStatus,
            String message,
            String processedBy) {
        return VideoStatusUpdateEvent.builder()
                .videoId(videoId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .message(message)
                .processedBy(processedBy)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
