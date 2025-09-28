package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.application.usecase.dto.VideoDownloadData;
import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobStorageService;
import br.com.fiap.videosapi.video.infrastructure.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoDownloadUseCaseImplTest {

    @Mock
    private VideoRepository videoRepository;
    @Mock
    private AzureBlobStorageService azureBlobStorageService;
    @InjectMocks
    private VideoDownloadUseCaseImpl videoDownloadUseCase;

    private Video video;

    @BeforeEach
    void setUp() {
        video = Video.builder()
                .id(1L)
                .originalFileName("video-teste.mp4")
                .storedFileName("stored-video-teste.mp4")
                .contentType("video/mp4")
                .fileSize(1024L)
                .azureBlobUrl("url-azure")
                .containerName("container")
                .build();
    }

    @Test
    @DisplayName("Deve preparar download com sucesso quando frames.zip existe")
    void devePrepararDownloadComSucesso() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));
        when(azureBlobStorageService.blobExists("cliente1/1/frames/frames.zip")).thenReturn(true);

        VideoDownloadData data = videoDownloadUseCase.prepareDownload(1L, "cliente1");

        assertNotNull(data);
        assertEquals(video, data.getVideo());
        assertEquals("cliente1/1/frames/frames.zip", data.getVideoBlobName());
        assertEquals("frames-video-teste.mp4.zip", data.getZipFileName());
        verify(videoRepository).findById(1L);
        verify(azureBlobStorageService).blobExists("cliente1/1/frames/frames.zip");
    }

    @Test
    @DisplayName("Deve lançar exceção quando vídeo não for encontrado")
    void deveLancarExcecaoQuandoVideoNaoEncontrado() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                videoDownloadUseCase.prepareDownload(99L, "cliente1")
        );
        assertEquals("Video not found with id: 99", ex.getMessage());
        verify(videoRepository).findById(99L);
        verify(azureBlobStorageService, never()).blobExists(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando frames.zip não existe")
    void deveLancarExcecaoQuandoFramesZipNaoExiste() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));
        when(azureBlobStorageService.blobExists("cliente1/1/frames/frames.zip")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                videoDownloadUseCase.prepareDownload(1L, "cliente1")
        );
        assertEquals("Arquivo frames.zip não encontrado para o vídeo: 1", ex.getMessage());
        verify(videoRepository).findById(1L);
        verify(azureBlobStorageService).blobExists("cliente1/1/frames/frames.zip");
    }
}

