package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.common.domain.dto.event.VideoUploadEvent;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobStorageService;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobUploadResult;
import br.com.fiap.videosapi.video.infrastructure.kafka.VideoEventProducer;
import br.com.fiap.videosapi.video.infrastructure.repository.VideoRepository;
import org.apache.tika.Tika;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VideoUploadUseCaseImplTest {

    @Mock
    private AzureBlobStorageService azureBlobStorageService;
    @Mock
    private VideoEventProducer videoEventProducer;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private MultipartFile multipartFile;
    @Mock
    private Tika tika;

    @InjectMocks
    private VideoUploadUseCaseImpl videoUploadUseCase;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        videoUploadUseCase = new VideoUploadUseCaseImpl(
                azureBlobStorageService, videoEventProducer, videoRepository);

        try {
            java.lang.reflect.Field field = VideoUploadUseCaseImpl.class.getDeclaredField("connectionString");
            field.setAccessible(true);
            field.set(videoUploadUseCase, "fake-connection-string");
        } catch (Exception ignored) {}

        try {
            java.lang.reflect.Field field = VideoUploadUseCaseImpl.class.getDeclaredField("tika");
            field.setAccessible(true);
            field.set(videoUploadUseCase, tika);
        } catch (Exception ignored) {}
    }

    @AfterEach
    void tearDown() throws Exception {
        this.autoCloseable.close();
    }

    @Test
    @DisplayName("Deve fazer upload de vídeo com sucesso")
    void deveFazerUploadDeVideoComSucesso() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1]));
        when(multipartFile.getOriginalFilename()).thenReturn("video.mp4");
        when(multipartFile.getContentType()).thenReturn("video/mp4");
        when(tika.detect(any(ByteArrayInputStream.class))).thenReturn("video/mp4");
        AzureBlobUploadResult uploadResult = AzureBlobUploadResult.builder()
                .success(true)
                .fileName("stored.mp4")
                .blobUrl("url")
                .containerName("container")
                .build();
        when(azureBlobStorageService.uploadVideo(any(), any())).thenReturn(uploadResult);
        Video video = Video.builder()
                .id(1L)
                .originalFileName("video.mp4")
                .storedFileName("stored.mp4")
                .contentType("video/mp4")
                .fileSize(100L)
                .azureBlobUrl("url")
                .containerName("container")
                .status(VideoStatus.UPLOADED)
                .uploadedAt(LocalDateTime.now())
                .build();
        when(videoRepository.save(any())).thenReturn(video);

        VideoUploadResponse response = videoUploadUseCase.uploadVideo(multipartFile, "user123");

        assertNotNull(response);
        assertEquals("video.mp4", response.getOriginalFileName());
        assertEquals("stored.mp4", response.getStoredFileName());
        assertEquals("video/mp4", response.getContentType());
        assertEquals(100L, response.getFileSize());
        assertEquals("url", response.getAzureBlobUrl());
        assertEquals(VideoStatus.UPLOADED, response.getStatus());
        assertTrue(response.getMessage().contains("sucesso") || response.getMessage().contains("success"));
        verify(videoEventProducer, times(1)).publishVideoUploadEvent(any(VideoUploadEvent.class));
    }

    @Test
    @DisplayName("Deve retornar erro para arquivo inválido (vazio)")
    void deveRetornarErroParaArquivoVazio() {
        when(multipartFile.isEmpty()).thenReturn(true);
        VideoUploadResponse response = videoUploadUseCase.uploadVideo(multipartFile, "user123");
        assertNotNull(response);
        assertTrue(response.getMessage().toLowerCase().contains("empty"));
    }

    @Test
    @DisplayName("Deve retornar erro para tipo de arquivo inválido")
    void deveRetornarErroParaTipoInvalido() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1]));
        when(tika.detect(any(ByteArrayInputStream.class))).thenReturn("image/png");
        VideoUploadResponse response = videoUploadUseCase.uploadVideo(multipartFile, "user123");
        assertNotNull(response);
        assertTrue(response.getMessage().contains("Invalid file type"));
    }

    @Test
    @DisplayName("Deve retornar erro para arquivo maior que o permitido")
    void deveRetornarErroParaArquivoGrande() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(600L * 1024 * 1024);
        VideoUploadResponse response = videoUploadUseCase.uploadVideo(multipartFile, "user123");
        assertNotNull(response);
        assertTrue(response.getMessage().contains("exceeds"));
    }

    @Test
    @DisplayName("Deve lançar exceção para lista de arquivos vazia")
    void deveLancarExcecaoParaListaVazia() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                videoUploadUseCase.uploadVideos(Collections.emptyList(), "user123"));
        assertTrue(ex.getMessage().contains("No files"));
    }
}
