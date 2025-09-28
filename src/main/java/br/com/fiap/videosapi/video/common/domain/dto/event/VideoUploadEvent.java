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
public class VideoUploadEvent {

    private Long videoId;
    private String originalFileName;
    private String storedFileName;
    private String contentType;
    private Long fileSize;
    private String azureBlobUrl;
    private String containerName;
    private String connectionString;
    private String userId;
    private VideoStatus status;
    private LocalDateTime uploadedAt;
    private String eventType;
    private String userId;

    public static VideoUploadEvent createUploadSuccessEvent(Long videoId, String originalFileName,
                                                            String storedFileName, String contentType,
                                                            Long fileSize, String azureBlobUrl,
                                                            String containerName, String connectionString,
                                                            LocalDateTime uploadedAt, String userId) {
        return VideoUploadEvent.builder()
                .videoId(videoId)
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .contentType(contentType)
                .fileSize(fileSize)
                .azureBlobUrl(azureBlobUrl)
                .containerName(containerName)
                .connectionString(connectionString)
                .userId(userId)
                .status(VideoStatus.UPLOADED)
                .uploadedAt(uploadedAt)
                .eventType("VIDEO_UPLOAD_SUCCESS")
                .build();
    }
}
