package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobStorageService;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobUploadResult;
import br.com.fiap.videosapi.video.infrastructure.kafka.VideoEventProducer;
import br.com.fiap.videosapi.video.infrastructure.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoUploadUseCaseImplTest {

    @Mock
    private AzureBlobStorageService azureBlobStorageService;

    @Mock
    private VideoEventProducer videoEventProducer;

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoUploadUseCaseImpl videoUploadUseCase;

    private MultipartFile validVideoFile;
    private AzureBlobUploadResult successfulUploadResult;
    private Video savedVideo;

    @BeforeEach
    void setUp() {
        // Create a valid MP4 file mock
        byte[] mp4Header = new byte[]{0x00, 0x00, 0x00, 0x20, 0x66, 0x74, 0x79, 0x70, 0x69, 0x73, 0x6F, 0x6D};
        validVideoFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                mp4Header
        );

        successfulUploadResult = AzureBlobUploadResult.builder()
                .fileName("stored-video.mp4")
                .blobUrl("https://storage.blob.core.windows.net/videos/stored-video.mp4")
                .containerName("videos")
                .fileSize(1024L)
                .contentType("video/mp4")
                .success(true)
                .build();

        savedVideo = Video.builder()
                .id(1L)
                .originalFileName("test-video.mp4")
                .storedFileName("stored-video.mp4")
                .contentType("video/mp4")
                .fileSize(1024L)
                .azureBlobUrl("https://storage.blob.core.windows.net/videos/stored-video.mp4")
                .containerName("videos")
                .status(VideoStatus.UPLOADED)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void uploadVideo_Success() {
        // Arrange
        when(azureBlobStorageService.uploadVideo(any(MultipartFile.class)))
                .thenReturn(successfulUploadResult);
        when(videoRepository.save(any(Video.class)))
                .thenReturn(savedVideo);

        // Act
        VideoUploadResponse response = videoUploadUseCase.uploadVideo(validVideoFile);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test-video.mp4", response.getOriginalFileName());
        assertEquals("stored-video.mp4", response.getStoredFileName());
        assertEquals("video/mp4", response.getContentType());
        assertEquals(1024L, response.getFileSize());
        assertEquals(VideoStatus.UPLOADED, response.getStatus());
        assertEquals("Video uploaded successfully", response.getMessage());

        verify(azureBlobStorageService).uploadVideo(validVideoFile);
        verify(videoRepository).save(any(Video.class));
        verify(videoEventProducer).publishVideoUploadEvent(any());
    }

    @Test
    void uploadVideo_EmptyFile_ThrowsException() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.mp4",
                "video/mp4",
                new byte[0]
        );

        // Act
        VideoUploadResponse response = videoUploadUseCase.uploadVideo(emptyFile);

        // Assert
        assertNotNull(response);
        assertNull(response.getId());
        assertTrue(response.getMessage().contains("Failed to upload video"));

        verify(azureBlobStorageService, never()).uploadVideo(any());
        verify(videoRepository, never()).save(any());
        verify(videoEventProducer, never()).publishVideoUploadEvent(any());
    }

    @Test
    void uploadVideo_InvalidFileType_ThrowsException() {
        // Arrange
        MultipartFile invalidFile = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "This is not a video file".getBytes()
        );

        // Act
        VideoUploadResponse response = videoUploadUseCase.uploadVideo(invalidFile);

        // Assert
        assertNotNull(response);
        assertNull(response.getId());
        assertTrue(response.getMessage().contains("Failed to upload video"));

        verify(azureBlobStorageService, never()).uploadVideo(any());
        verify(videoRepository, never()).save(any());
        verify(videoEventProducer, never()).publishVideoUploadEvent(any());
    }

    @Test
    void uploadVideo_AzureUploadFails_ReturnsErrorResponse() {
        // Arrange
        AzureBlobUploadResult failedUploadResult = AzureBlobUploadResult.builder()
                .success(false)
                .errorMessage("Azure storage error")
                .build();

        when(azureBlobStorageService.uploadVideo(any(MultipartFile.class)))
                .thenReturn(failedUploadResult);

        // Act
        VideoUploadResponse response = videoUploadUseCase.uploadVideo(validVideoFile);

        // Assert
        assertNotNull(response);
        assertNull(response.getId());
        assertTrue(response.getMessage().contains("Failed to upload video"));

        verify(azureBlobStorageService).uploadVideo(validVideoFile);
        verify(videoRepository, never()).save(any());
        verify(videoEventProducer, never()).publishVideoUploadEvent(any());
    }
}
