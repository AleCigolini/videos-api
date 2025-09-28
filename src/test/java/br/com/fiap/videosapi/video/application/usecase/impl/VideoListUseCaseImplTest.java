package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.common.domain.dto.response.VideoListResponse;
import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.infrastructure.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
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
import br.com.fiap.videosapi.core.context.UserContext;

@ExtendWith(MockitoExtension.class)
class VideoListUseCaseImplTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoListUseCaseImpl videoListUseCase;

    private Video videoProcessado;
    private Video videoPendente;
    private Video videoComErro;
    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        UserContext.setUserId(USER_ID);
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

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("Deve listar todos os vídeos com sucesso")
    void deveListarTodosOsVideosComSucesso() {
        List<Video> videos = Arrays.asList(videoProcessado, videoPendente, videoComErro);
        when(videoRepository.findAllByUserId(USER_ID)).thenReturn(videos);

        List<VideoListResponse> resultado = videoListUseCase.listAllVideos();

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals(videoProcessado.getId(), resultado.get(0).getId());
        assertEquals(videoPendente.getId(), resultado.get(1).getId());
        assertEquals(videoComErro.getId(), resultado.get(2).getId());
        verify(videoRepository).findAllByUserId(USER_ID);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver vídeos")
    void deveRetornarListaVaziaQuandoNaoHouverVideos() {
        when(videoRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());

        List<VideoListResponse> resultado = videoListUseCase.listAllVideos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(videoRepository).findAllByUserId(USER_ID);
    }

    @Test
    @DisplayName("Deve listar vídeos por status PROCESSED com sucesso")
    void deveListarVideosPorStatusProcessedComSucesso() {
        List<Video> videosProcessados = Collections.singletonList(videoProcessado);
        when(videoRepository.findByStatusAndUserId(VideoStatus.PROCESSED, USER_ID)).thenReturn(videosProcessados);

        List<VideoListResponse> resultado = videoListUseCase.listVideosByStatus(VideoStatus.PROCESSED);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(VideoStatus.PROCESSED, resultado.getFirst().getStatus());
        assertNotNull(resultado.getFirst().getDownloadUrl());
        verify(videoRepository).findByStatusAndUserId(VideoStatus.PROCESSED, USER_ID);
    }

    @Test
    @DisplayName("Deve listar vídeos por status PROCESSING com sucesso")
    void deveListarVideosPorStatusPendingComSucesso() {
        List<Video> videosPendentes = Collections.singletonList(videoPendente);
        when(videoRepository.findByStatusAndUserId(VideoStatus.PROCESSING, USER_ID)).thenReturn(videosPendentes);

        List<VideoListResponse> resultado = videoListUseCase.listVideosByStatus(VideoStatus.PROCESSING);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(VideoStatus.PROCESSING, resultado.getFirst().getStatus());
        assertNull(resultado.getFirst().getDownloadUrl());
        verify(videoRepository).findByStatusAndUserId(VideoStatus.PROCESSING, USER_ID);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver vídeos com status específico")
    void deveRetornarListaVaziaQuandoNaoHouverVideosComStatusEspecifico() {
        when(videoRepository.findByStatusAndUserId(VideoStatus.PROCESSING, USER_ID)).thenReturn(Collections.emptyList());

        List<VideoListResponse> resultado = videoListUseCase.listVideosByStatus(VideoStatus.PROCESSING);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(videoRepository).findByStatusAndUserId(VideoStatus.PROCESSING, USER_ID);
    }

    @Test
    @DisplayName("Deve buscar vídeo por ID com sucesso")
    void deveBuscarVideoPorIdComSucesso() {
        when(videoRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(videoProcessado));

        VideoListResponse resultado = videoListUseCase.getVideoById(1L);

        assertNotNull(resultado);
        assertEquals(videoProcessado.getId(), resultado.getId());
        assertEquals(videoProcessado.getOriginalFileName(), resultado.getOriginalFileName());
        assertEquals(videoProcessado.getStatus(), resultado.getStatus());
        assertEquals(videoProcessado.getFileSize(), resultado.getFileSize());
        assertNotNull(resultado.getDownloadUrl());
        verify(videoRepository).findByIdAndUserId(1L, USER_ID);
    }

    @Test
    @DisplayName("Deve lançar exceção quando vídeo não for encontrado por ID")
    void deveLancarExcecaoQuandoVideoNaoForEncontradoPorId() {
        when(videoRepository.findByIdAndUserId(999L, USER_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoListUseCase.getVideoById(999L)
        );

        assertEquals("Video not found with ID: 999", exception.getMessage());
        verify(videoRepository).findByIdAndUserId(999L, USER_ID);
    }

    @Test
    @DisplayName("Deve mapear vídeo processado com URL de download")
    void deveMaperarVideoProcessadoComUrlDeDownload() {
        when(videoRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(videoProcessado));

        VideoListResponse resultado = videoListUseCase.getVideoById(1L);

        assertEquals(videoProcessado.getAzureBlobUrl(), resultado.getDownloadUrl());
        assertEquals(VideoStatus.PROCESSED, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve mapear vídeo com erro sem URL de download")
    void deveMaperarVideoComErroSemUrlDeDownload() {
        when(videoRepository.findByIdAndUserId(3L, USER_ID)).thenReturn(Optional.of(videoComErro));

        VideoListResponse resultado = videoListUseCase.getVideoById(3L);

        assertNull(resultado.getDownloadUrl());
        assertEquals(VideoStatus.FAILED, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve preservar todos os campos do vídeo no mapeamento")
    void devePreservarTodosOsCamposDoVideoNoMapeamento() {
        when(videoRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(videoProcessado));

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
        when(videoRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());
        when(videoRepository.findByStatusAndUserId(eq(VideoStatus.PROCESSED), eq(USER_ID))).thenReturn(Collections.emptyList());
        when(videoRepository.findByIdAndUserId(eq(1L), eq(USER_ID))).thenReturn(Optional.of(videoProcessado));

        videoListUseCase.listAllVideos();
        videoListUseCase.listVideosByStatus(VideoStatus.PROCESSED);
        videoListUseCase.getVideoById(1L);

        verify(videoRepository, times(1)).findAllByUserId(USER_ID);
        verify(videoRepository, times(1)).findByStatusAndUserId(VideoStatus.PROCESSED, USER_ID);
        verify(videoRepository, times(1)).findByIdAndUserId(1L, USER_ID);
    }
}
