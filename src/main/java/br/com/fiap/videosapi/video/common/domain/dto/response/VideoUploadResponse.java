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
public class VideoUploadResponse {

    private Long id;
    private String originalFileName;
    private String storedFileName;
    private String contentType;
    private Long fileSize;
    private String azureBlobUrl;
    private VideoStatus status;
    private LocalDateTime uploadedAt;
    private String message;
}
