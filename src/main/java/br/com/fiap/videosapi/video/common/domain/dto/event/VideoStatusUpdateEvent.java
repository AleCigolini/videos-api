package br.com.fiap.videosapi.video.common.domain.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoStatusUpdateEvent {

    private Long videoId;
    private String userId;
    private String status;
}
