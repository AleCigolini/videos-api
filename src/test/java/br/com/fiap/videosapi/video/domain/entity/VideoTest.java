package br.com.fiap.videosapi.video.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VideoTest {

    private Video video;
    private LocalDateTime dataFixa;

    @BeforeEach
    void setUp() {
        dataFixa = LocalDateTime.of(2025, 9, 25, 10, 30, 0);

        video = Video.builder()
                .id(1L)
                .originalFileName("video-teste.mp4")
                .storedFileName("stored-video-123.mp4")
                .contentType("video/mp4")
                .fileSize(1024L)
                .azureBlobUrl("https://storage.blob.core.windows.net/videos/stored-video-123.mp4")
                .containerName("videos-container")
                .status(VideoStatus.UPLOADED)
                .uploadedAt(dataFixa)
                .processedAt(null)
                .build();
    }

    @Test
    @DisplayName("Deve criar video usando builder com todos os campos")
    void deveCriarVideoUsandoBuilderComTodosOsCampos() {
        Video novoVideo = Video.builder()
                .id(2L)
                .originalFileName("novo-video.mp4")
                .storedFileName("stored-novo-123.mp4")
                .contentType("video/avi")
                .fileSize(2048L)
                .azureBlobUrl("https://test.com/novo-video.mp4")
                .containerName("test-container")
                .status(VideoStatus.PROCESSED)
                .uploadedAt(dataFixa)
                .processedAt(dataFixa.plusHours(1))
                .build();

        assertEquals(2L, novoVideo.getId());
        assertEquals("novo-video.mp4", novoVideo.getOriginalFileName());
        assertEquals("stored-novo-123.mp4", novoVideo.getStoredFileName());
        assertEquals("video/avi", novoVideo.getContentType());
        assertEquals(2048L, novoVideo.getFileSize());
        assertEquals("https://test.com/novo-video.mp4", novoVideo.getAzureBlobUrl());
        assertEquals("test-container", novoVideo.getContainerName());
        assertEquals(VideoStatus.PROCESSED, novoVideo.getStatus());
        assertEquals(dataFixa, novoVideo.getUploadedAt());
        assertEquals(dataFixa.plusHours(1), novoVideo.getProcessedAt());
    }

    @Test
    @DisplayName("Deve criar video usando construtor sem argumentos")
    void deveCriarVideoUsandoConstrutorSemArgumentos() {
        Video videoVazio = new Video();

        assertNull(videoVazio.getId());
        assertNull(videoVazio.getOriginalFileName());
        assertNull(videoVazio.getStoredFileName());
        assertNull(videoVazio.getContentType());
        assertNull(videoVazio.getFileSize());
        assertNull(videoVazio.getAzureBlobUrl());
        assertNull(videoVazio.getContainerName());
        assertNull(videoVazio.getStatus());
        assertNull(videoVazio.getUploadedAt());
        assertNull(videoVazio.getProcessedAt());
    }

    @Test
    @DisplayName("Deve executar onCreate quando status for nulo")
    void deveExecutarOnCreateQuandoStatusForNulo() {
        Video videoSemStatus = new Video();
        LocalDateTime antesOnCreate = LocalDateTime.now();

        videoSemStatus.onCreate();

        LocalDateTime depoisOnCreate = LocalDateTime.now();

        assertNotNull(videoSemStatus.getUploadedAt());
        assertEquals(VideoStatus.UPLOADED, videoSemStatus.getStatus());
        assertTrue(videoSemStatus.getUploadedAt().isAfter(antesOnCreate) ||
                  videoSemStatus.getUploadedAt().isEqual(antesOnCreate));
        assertTrue(videoSemStatus.getUploadedAt().isBefore(depoisOnCreate) ||
                  videoSemStatus.getUploadedAt().isEqual(depoisOnCreate));
    }

    @Test
    @DisplayName("Deve executar onCreate mantendo status existente")
    void deveExecutarOnCreateMantendoStatusExistente() {
        Video videoComStatus = new Video();
        videoComStatus.setStatus(VideoStatus.PROCESSED);
        LocalDateTime antesOnCreate = LocalDateTime.now();

        videoComStatus.onCreate();

        LocalDateTime depoisOnCreate = LocalDateTime.now();

        assertNotNull(videoComStatus.getUploadedAt());
        assertEquals(VideoStatus.PROCESSED, videoComStatus.getStatus());
        assertTrue(videoComStatus.getUploadedAt().isAfter(antesOnCreate) ||
                  videoComStatus.getUploadedAt().isEqual(antesOnCreate));
        assertTrue(videoComStatus.getUploadedAt().isBefore(depoisOnCreate) ||
                  videoComStatus.getUploadedAt().isEqual(depoisOnCreate));
    }

    @Test
    @DisplayName("Deve retornar true para equals com videos iguais")
    void deveRetornarTrueParaEqualsComVideosIguais() {
        Video video1 = Video.builder()
                .id(1L)
                .originalFileName("mesmo-video.mp4")
                .storedFileName("same-stored.mp4")
                .contentType("video/mp4")
                .fileSize(1024L)
                .azureBlobUrl("https://same.com/video.mp4")
                .containerName("same-container")
                .status(VideoStatus.UPLOADED)
                .uploadedAt(dataFixa)
                .build();

        Video video2 = Video.builder()
                .id(1L)
                .originalFileName("mesmo-video.mp4")
                .storedFileName("same-stored.mp4")
                .contentType("video/mp4")
                .fileSize(1024L)
                .azureBlobUrl("https://same.com/video.mp4")
                .containerName("same-container")
                .status(VideoStatus.UPLOADED)
                .uploadedAt(dataFixa)
                .build();

        assertEquals(video1, video2);
        assertEquals(video1.hashCode(), video2.hashCode());
    }

    @Test
    @DisplayName("Deve retornar false para equals com videos diferentes")
    void deveRetornarFalseParaEqualsComVideosDiferentes() {
        Video video1 = Video.builder()
                .id(1L)
                .originalFileName("video1.mp4")
                .status(VideoStatus.UPLOADED)
                .build();

        Video video2 = Video.builder()
                .id(2L)
                .originalFileName("video2.mp4")
                .status(VideoStatus.PROCESSED)
                .build();

        assertNotEquals(video1, video2);
        assertNotEquals(video1.hashCode(), video2.hashCode());
    }

    @Test
    @DisplayName("Deve gerar toString adequado")
    void deveGerarToStringAdequado() {
        String toStringResult = video.toString();

        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("Video"));
        assertTrue(toStringResult.contains("video-teste.mp4"));
        assertTrue(toStringResult.contains("UPLOADED"));
        assertTrue(toStringResult.contains("1024"));
    }

    @Test
    @DisplayName("Deve definir processedAt para video processado")
    void deveDefinirProcessedAtParaVideoProcessado() {
        LocalDateTime dataProcessamento = LocalDateTime.now();

        Video videoProcessado = Video.builder()
                .originalFileName("processado.mp4")
                .status(VideoStatus.PROCESSED)
                .processedAt(dataProcessamento)
                .build();

        assertEquals(dataProcessamento, videoProcessado.getProcessedAt());
        assertEquals(VideoStatus.PROCESSED, videoProcessado.getStatus());
    }

    @Test
    @DisplayName("Deve manter todos os campos obrigatórios preenchidos")
    void deveManterTodosCamposObrigatoriosPreenchidos() {
        assertNotNull(video.getOriginalFileName());
        assertNotNull(video.getStoredFileName());
        assertNotNull(video.getContentType());
        assertNotNull(video.getFileSize());
        assertNotNull(video.getAzureBlobUrl());
        assertNotNull(video.getContainerName());
        assertNotNull(video.getStatus());
        assertNotNull(video.getUploadedAt());
    }

    @Test
    @DisplayName("Deve aceitar diferentes tipos de arquivo de video")
    void deveAceitarDiferentesTiposDeArquivoDeVideo() {
        Video videoMp4 = Video.builder().contentType("video/mp4").build();
        Video videoAvi = Video.builder().contentType("video/avi").build();
        Video videoMov = Video.builder().contentType("video/mov").build();
        Video videoWebm = Video.builder().contentType("video/webm").build();

        assertEquals("video/mp4", videoMp4.getContentType());
        assertEquals("video/avi", videoAvi.getContentType());
        assertEquals("video/mov", videoMov.getContentType());
        assertEquals("video/webm", videoWebm.getContentType());
    }

    @Test
    @DisplayName("Deve aceitar diferentes tamanhos de arquivo")
    void deveAceitarDiferentesTamanhosDeArquivo() {
        Video videoPequeno = Video.builder().fileSize(1024L).build();
        Video videoMedio = Video.builder().fileSize(1048576L).build();
        Video videoGrande = Video.builder().fileSize(104857600L).build();

        assertEquals(1024L, videoPequeno.getFileSize());
        assertEquals(1048576L, videoMedio.getFileSize());
        assertEquals(104857600L, videoGrande.getFileSize());
    }

}
