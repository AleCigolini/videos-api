package br.com.fiap.videosapi.video.common.domain.dto.event;

import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VideoUploadEventTest {

    @Test
    @DisplayName("Deve criar VideoUploadEvent corretamente com createUploadSuccessEvent")
    void deveCriarVideoUploadEventComFactoryMethod() {
        Long videoId = 10L;
        String originalFileName = "video.mp4";
        String storedFileName = "stored_video.mp4";
        String contentType = "video/mp4";
        Long fileSize = 123456L;
        String azureBlobUrl = "https://blob.core.windows.net/container/stored_video.mp4";
        String containerName = "container";
        String connectionString = "DefaultEndpointsProtocol=https;...";
        LocalDateTime uploadedAt = LocalDateTime.now();

        VideoUploadEvent event = VideoUploadEvent.createUploadSuccessEvent(
                videoId, originalFileName, storedFileName, contentType, fileSize,
                azureBlobUrl, containerName, connectionString, uploadedAt
        );

        assertNotNull(event);
        assertEquals(videoId, event.getVideoId());
        assertEquals(originalFileName, event.getOriginalFileName());
        assertEquals(storedFileName, event.getStoredFileName());
        assertEquals(contentType, event.getContentType());
        assertEquals(fileSize, event.getFileSize());
        assertEquals(azureBlobUrl, event.getAzureBlobUrl());
        assertEquals(containerName, event.getContainerName());
        assertEquals(connectionString, event.getConnectionString());
        assertEquals(VideoStatus.UPLOADED, event.getStatus());
        assertEquals(uploadedAt, event.getUploadedAt());
        assertEquals("VIDEO_UPLOAD_SUCCESS", event.getEventType());
    }

    @Test
    @DisplayName("Deve permitir construção via builder e getters/setters do Lombok")
    void devePermitirBuilderELombok() {
        LocalDateTime now = LocalDateTime.now();
        VideoUploadEvent event = VideoUploadEvent.builder()
                .videoId(1L)
                .originalFileName("file.mp4")
                .storedFileName("stored.mp4")
                .contentType("video/mp4")
                .fileSize(100L)
                .azureBlobUrl("url")
                .containerName("container")
                .connectionString("conn")
                .status(VideoStatus.UPLOADED)
                .uploadedAt(now)
                .eventType("TYPE")
                .build();

        assertEquals(1L, event.getVideoId());
        assertEquals("file.mp4", event.getOriginalFileName());
        assertEquals("stored.mp4", event.getStoredFileName());
        assertEquals("video/mp4", event.getContentType());
        assertEquals(100L, event.getFileSize());
        assertEquals("url", event.getAzureBlobUrl());
        assertEquals("container", event.getContainerName());
        assertEquals("conn", event.getConnectionString());
        assertEquals(VideoStatus.UPLOADED, event.getStatus());
        assertEquals(now, event.getUploadedAt());
        assertEquals("TYPE", event.getEventType());
    }
}

