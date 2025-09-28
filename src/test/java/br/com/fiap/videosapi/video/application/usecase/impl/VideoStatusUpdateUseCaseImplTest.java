package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.common.domain.dto.event.VideoStatusUpdateEvent;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoStatusUpdateUseCaseImplTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoStatusUpdateUseCaseImpl videoStatusUpdateUseCase;

    private Video videoPendente;
    private Video videoProcessando;
    private VideoStatusUpdateEvent eventoSucesso;
    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        videoPendente = Video.builder()
                .id(1L)
                .originalFileName("video-teste.mp4")
                .status(VideoStatus.PROCESSING)
                .fileSize(1024L)
                .uploadedAt(LocalDateTime.now().minusHours(1))
                .userId(USER_ID)
                .build();

        videoProcessando = Video.builder()
                .id(2L)
                .originalFileName("video-processando.mp4")
                .status(VideoStatus.PROCESSING)
                .fileSize(2048L)
                .uploadedAt(LocalDateTime.now().minusHours(2))
                .userId(USER_ID)
                .build();

        eventoSucesso = VideoStatusUpdateEvent.builder()
                .videoId(1L)
                .userId(USER_ID)
                .status("SUCCESS")
                .build();
    }

    @Test
    @DisplayName("Deve definir processedAt quando status for PROCESSED")
    void deveDefinirProcessedAtQuandoStatusForProcessed() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(videoPendente));
        when(videoRepository.save(any(Video.class))).thenReturn(videoPendente);

        videoStatusUpdateUseCase.updateVideoStatus(1L, "SUCCESS");

        verify(videoRepository).save(videoPendente);
        assertEquals(VideoStatus.PROCESSED, videoPendente.getStatus());
        assertNotNull(videoPendente.getProcessedAt());
    }

    @Test
    @DisplayName("Deve não definir processedAt para outros status")
    void deveNaoDefinirProcessedAtParaOutrosStatus() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(videoPendente));
        when(videoRepository.save(any(Video.class))).thenReturn(videoPendente);

        videoStatusUpdateUseCase.updateVideoStatus(1L, "ERROR");

        verify(videoRepository).save(videoPendente);
        assertEquals(VideoStatus.FAILED, videoPendente.getStatus());
        assertNull(videoPendente.getProcessedAt());
    }

    @Test
    @DisplayName("Deve lançar exceção quando vídeo não for encontrado")
    void deveLancarExcecaoQuandoVideoNaoForEncontrado() {
        when(videoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoStatusUpdateUseCase.updateVideoStatus(999L, "SUCCESS")
        );

        assertEquals("Video not found with ID: 999", exception.getMessage());
        verify(videoRepository).findById(999L);
        verify(videoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve processar evento de atualização de status com sucesso")
    void deveProcessarEventoDeAtualizacaoDeStatusComSucesso() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(videoPendente));
        when(videoRepository.save(any(Video.class))).thenReturn(videoPendente);

        videoStatusUpdateUseCase.processStatusUpdateEvent(eventoSucesso);

        verify(videoRepository).save(videoPendente);
        assertEquals(VideoStatus.PROCESSED, videoPendente.getStatus());
        assertNotNull(videoPendente.getProcessedAt());
    }

    @Test
    @DisplayName("Deve propagar exceção ao processar evento com vídeo inexistente")
    void devePropagarExcecaoAoProcessarEventoComVideoInexistente() {
        when(videoRepository.findById(999L)).thenReturn(Optional.empty());

        VideoStatusUpdateEvent eventoInvalido = VideoStatusUpdateEvent.builder()
                .videoId(999L)
                .userId(USER_ID)
                .status("ERROR")
                .build();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoStatusUpdateUseCase.processStatusUpdateEvent(eventoInvalido)
        );

        assertEquals("Video not found with ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar status de PROCESSING para PROCESSED")
    void deveAtualizarStatusDeProcessingParaProcessed() {
        when(videoRepository.findById(2L)).thenReturn(Optional.of(videoProcessando));
        when(videoRepository.save(any(Video.class))).thenReturn(videoProcessando);

        videoStatusUpdateUseCase.updateVideoStatus(2L, "SUCCESS");

        assertEquals(VideoStatus.PROCESSED, videoProcessando.getStatus());
        assertNotNull(videoProcessando.getProcessedAt());
        verify(videoRepository).save(videoProcessando);
    }

    @Test
    @DisplayName("Deve atualizar status de PROCESSING para ERROR")
    void deveAtualizarStatusDeProcessingParaError() {
        when(videoRepository.findById(2L)).thenReturn(Optional.of(videoProcessando));
        when(videoRepository.save(any(Video.class))).thenReturn(videoProcessando);

        videoStatusUpdateUseCase.updateVideoStatus(2L, "ERROR");

        assertEquals(VideoStatus.FAILED, videoProcessando.getStatus());
        assertNull(videoProcessando.getProcessedAt());
        verify(videoRepository).save(videoProcessando);
    }

    @Test
    @DisplayName("Deve manter dados originais do vídeo após atualização")
    void deveManterDadosOriginaisDoVideoAposAtualizacao() {
        String nomeOriginal = videoPendente.getOriginalFileName();
        Long tamanhoOriginal = videoPendente.getFileSize();
        LocalDateTime uploadOriginal = videoPendente.getUploadedAt();

        when(videoRepository.findById(1L)).thenReturn(Optional.of(videoPendente));
        when(videoRepository.save(any(Video.class))).thenReturn(videoPendente);

        videoStatusUpdateUseCase.updateVideoStatus(1L, "SUCCESS");

        assertEquals(nomeOriginal, videoPendente.getOriginalFileName());
        assertEquals(tamanhoOriginal, videoPendente.getFileSize());
        assertEquals(uploadOriginal, videoPendente.getUploadedAt());
        assertEquals(VideoStatus.PROCESSED, videoPendente.getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção quando userId do evento divergir do userId do vídeo")
    void deveLancarExcecaoQuandoUserIdDivergir() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(videoPendente));

        VideoStatusUpdateEvent eventoUserDivergente = VideoStatusUpdateEvent.builder()
                .videoId(1L)
                .userId("outro-user")
                .status("SUCCESS")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                videoStatusUpdateUseCase.processStatusUpdateEvent(eventoUserDivergente));
        assertTrue(ex.getMessage().contains("User mismatch"));
        verify(videoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve chamar repository findById e save exatamente uma vez")
    void deveChamarRepositoryFindByIdESaveExatamenteUmaVez() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(videoPendente));
        when(videoRepository.save(any(Video.class))).thenReturn(videoPendente);

        videoStatusUpdateUseCase.updateVideoStatus(1L, "SUCCESS");

        verify(videoRepository, times(1)).findById(1L);
        verify(videoRepository, times(1)).save(videoPendente);
    }
}
