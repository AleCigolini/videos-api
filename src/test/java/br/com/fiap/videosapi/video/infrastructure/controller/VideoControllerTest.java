package br.com.fiap.videosapi.video.infrastructure.controller;

import br.com.fiap.videosapi.video.application.usecase.VideoUploadUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoController.class)
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoUploadUseCase videoUploadUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadVideo_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "video content".getBytes()
        );

        VideoUploadResponse successResponse = VideoUploadResponse.builder()
                .id(1L)
                .originalFileName("test-video.mp4")
                .storedFileName("stored-video.mp4")
                .contentType("video/mp4")
                .fileSize(1024L)
                .azureBlobUrl("https://storage.blob.core.windows.net/videos/stored-video.mp4")
                .status(VideoStatus.UPLOADED)
                .uploadedAt(LocalDateTime.now())
                .message("Video uploaded successfully")
                .build();

        when(videoUploadUseCase.uploadVideo(any())).thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/videos/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.originalFileName").value("test-video.mp4"))
                .andExpect(jsonPath("$.message").value("Video uploaded successfully"));
    }

    @Test
    void uploadVideo_Failure() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "video content".getBytes()
        );

        VideoUploadResponse errorResponse = VideoUploadResponse.builder()
                .message("Failed to upload video: Invalid file type")
                .build();

        when(videoUploadUseCase.uploadVideo(any())).thenReturn(errorResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/videos/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Failed to upload video: Invalid file type"));
    }

    @Test
    void healthCheck_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/videos/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Video service is running"));
    }
}
