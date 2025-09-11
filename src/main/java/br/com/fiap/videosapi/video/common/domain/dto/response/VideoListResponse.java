package br.com.fiap.videosapi.video.common.domain.dto.response;

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
public class VideoListResponse {

    private Long id;
    private String originalFileName;
    private VideoStatus status;
    private Long fileSize;
    private String downloadUrl;
    private LocalDateTime uploadedAt;
    private LocalDateTime processedAt;
}
