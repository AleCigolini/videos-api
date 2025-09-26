package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.common.domain.dto.response.VideoListResponse;
import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.infrastructure.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoListUseCaseImplTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoListUseCaseImpl videoListUseCase;

    private Video videoProcessado;
    private Video videoPendente;
    private Video videoComErro;

    @BeforeEach
    void setUp() {
        videoProcessado = Video.builder()
                .id(1L)
                .originalFileName("video1.mp4")
                .status(VideoStatus.PROCESSED)
                .fileSize(1024L)
                .azureBlobUrl("https://storage.blob.core.windows.net/videos/video1.mp4")
                .uploadedAt(LocalDateTime.now().minusHours(2))
                .processedAt(LocalDateTime.now().minusHours(1))
                .build();

        videoPendente = Video.builder()
                .id(2L)
                .originalFileName("video2.mp4")
                .status(VideoStatus.PROCESSING)
                .fileSize(2048L)
                .uploadedAt(LocalDateTime.now().minusHours(1))
                .build();

        videoComErro = Video.builder()
                .id(3L)
                .originalFileName("video3.mp4")
                .status(VideoStatus.FAILED)
                .fileSize(512L)
                .uploadedAt(LocalDateTime.now().minusMinutes(30))
                .build();
    }

    @Test
    @DisplayName("Deve listar todos os vídeos com sucesso")
    void deveListarTodosOsVideosComSucesso() {
        List<Video> videos = Arrays.asList(videoProcessado, videoPendente, videoComErro);
        when(videoRepository.findAll()).thenReturn(videos);

        List<VideoListResponse> resultado = videoListUseCase.listAllVideos();

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals(videoProcessado.getId(), resultado.get(0).getId());
        assertEquals(videoPendente.getId(), resultado.get(1).getId());
        assertEquals(videoComErro.getId(), resultado.get(2).getId());
        verify(videoRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver vídeos")
    void deveRetornarListaVaziaQuandoNaoHouverVideos() {
        when(videoRepository.findAll()).thenReturn(Collections.emptyList());

        List<VideoListResponse> resultado = videoListUseCase.listAllVideos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(videoRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar vídeos por status PROCESSED com sucesso")
    void deveListarVideosPorStatusProcessedComSucesso() {
        List<Video> videosProcessados = Collections.singletonList(videoProcessado);
        when(videoRepository.findByStatus(VideoStatus.PROCESSED)).thenReturn(videosProcessados);

        List<VideoListResponse> resultado = videoListUseCase.listVideosByStatus(VideoStatus.PROCESSED);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(VideoStatus.PROCESSED, resultado.getFirst().getStatus());
        assertNotNull(resultado.getFirst().getDownloadUrl());
        verify(videoRepository).findByStatus(VideoStatus.PROCESSED);
    }

    @Test
    @DisplayName("Deve listar vídeos por status PROCESSING com sucesso")
    void deveListarVideosPorStatusPendingComSucesso() {
        List<Video> videosPendentes = Collections.singletonList(videoPendente);
        when(videoRepository.findByStatus(VideoStatus.PROCESSING)).thenReturn(videosPendentes);

        List<VideoListResponse> resultado = videoListUseCase.listVideosByStatus(VideoStatus.PROCESSING);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(VideoStatus.PROCESSING, resultado.getFirst().getStatus());
        assertNull(resultado.getFirst().getDownloadUrl());
        verify(videoRepository).findByStatus(VideoStatus.PROCESSING);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver vídeos com status específico")
    void deveRetornarListaVaziaQuandoNaoHouverVideosComStatusEspecifico() {
        when(videoRepository.findByStatus(VideoStatus.PROCESSING)).thenReturn(Collections.emptyList());

        List<VideoListResponse> resultado = videoListUseCase.listVideosByStatus(VideoStatus.PROCESSING);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(videoRepository).findByStatus(VideoStatus.PROCESSING);
    }

    @Test
    @DisplayName("Deve buscar vídeo por ID com sucesso")
    void deveBuscarVideoPorIdComSucesso() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(videoProcessado));

        VideoListResponse resultado = videoListUseCase.getVideoById(1L);

        assertNotNull(resultado);
        assertEquals(videoProcessado.getId(), resultado.getId());
        assertEquals(videoProcessado.getOriginalFileName(), resultado.getOriginalFileName());
        assertEquals(videoProcessado.getStatus(), resultado.getStatus());
        assertEquals(videoProcessado.getFileSize(), resultado.getFileSize());
        assertNotNull(resultado.getDownloadUrl());
        verify(videoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando vídeo não for encontrado por ID")
    void deveLancarExcecaoQuandoVideoNaoForEncontradoPorId() {
        when(videoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoListUseCase.getVideoById(999L)
        );

        assertEquals("Video not found with ID: 999", exception.getMessage());
        verify(videoRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve mapear vídeo processado com URL de download")
    void deveMaperarVideoProcessadoComUrlDeDownload() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(videoProcessado));

        VideoListResponse resultado = videoListUseCase.getVideoById(1L);

        assertEquals(videoProcessado.getAzureBlobUrl(), resultado.getDownloadUrl());
        assertEquals(VideoStatus.PROCESSED, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve mapear vídeo com erro sem URL de download")
    void deveMaperarVideoComErroSemUrlDeDownload() {
        when(videoRepository.findById(3L)).thenReturn(Optional.of(videoComErro));

        VideoListResponse resultado = videoListUseCase.getVideoById(3L);

        assertNull(resultado.getDownloadUrl());
        assertEquals(VideoStatus.FAILED, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve preservar todos os campos do vídeo no mapeamento")
    void devePreservarTodosOsCamposDoVideoNoMapeamento() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(videoProcessado));

        VideoListResponse resultado = videoListUseCase.getVideoById(1L);

        assertEquals(videoProcessado.getId(), resultado.getId());
        assertEquals(videoProcessado.getOriginalFileName(), resultado.getOriginalFileName());
        assertEquals(videoProcessado.getStatus(), resultado.getStatus());
        assertEquals(videoProcessado.getFileSize(), resultado.getFileSize());
        assertEquals(videoProcessado.getUploadedAt(), resultado.getUploadedAt());
        assertEquals(videoProcessado.getProcessedAt(), resultado.getProcessedAt());
    }

    @Test
    @DisplayName("Deve chamar repository uma vez para cada método")
    void deveChamarRepositoryUmaVezParaCadaMetodo() {
        when(videoRepository.findAll()).thenReturn(Collections.emptyList());
        when(videoRepository.findByStatus(any())).thenReturn(Collections.emptyList());
        when(videoRepository.findById(any())).thenReturn(Optional.of(videoProcessado));

        videoListUseCase.listAllVideos();
        videoListUseCase.listVideosByStatus(VideoStatus.PROCESSED);
        videoListUseCase.getVideoById(1L);

        verify(videoRepository, times(1)).findAll();
        verify(videoRepository, times(1)).findByStatus(VideoStatus.PROCESSED);
        verify(videoRepository, times(1)).findById(1L);
    }
}
