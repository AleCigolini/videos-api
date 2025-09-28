package br.com.fiap.videosapi.video.application.usecase.dto;

import br.com.fiap.videosapi.video.domain.entity.Video;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VideoDownloadDataTest {

    @Test
    @DisplayName("Deve construir VideoDownloadData corretamente com builder")
    void deveConstruirComBuilder() {
        Video video = Video.builder()
                .id(1L)
                .originalFileName("video.mp4")
                .storedFileName("stored-video.mp4")
                .contentType("video/mp4")
                .fileSize(1234L)
                .azureBlobUrl("url-azure")
                .containerName("container")
                .build();

        VideoDownloadData data = VideoDownloadData.builder()
                .video(video)
                .videoBlobName("cliente1/1/frames/frames.zip")
                .zipFileName("frames-video.mp4.zip")
                .build();

        assertNotNull(data);
        assertEquals(video, data.getVideo());
        assertEquals("cliente1/1/frames/frames.zip", data.getVideoBlobName());
        assertEquals("frames-video.mp4.zip", data.getZipFileName());
    }

    @Test
    @DisplayName("Deve permitir alteração dos campos via setters")
    void devePermitirAlteracaoViaSetters() {
        VideoDownloadData data = new VideoDownloadData();
        Video video = Video.builder().id(2L).originalFileName("outro.mp4").build();
        data.setVideo(video);
        data.setVideoBlobName("cliente2/2/frames/frames.zip");
        data.setZipFileName("frames-outro.mp4.zip");

        assertEquals(video, data.getVideo());
        assertEquals("cliente2/2/frames/frames.zip", data.getVideoBlobName());
        assertEquals("frames-outro.mp4.zip", data.getZipFileName());
    }

    @Test
    @DisplayName("Deve permitir criar objeto vazio e preencher depois")
    void devePermitirCriarVazioEPreencherDepois() {
        VideoDownloadData data = new VideoDownloadData();
        assertNull(data.getVideo());
        assertNull(data.getVideoBlobName());
        assertNull(data.getZipFileName());
    }
}

