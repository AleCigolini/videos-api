package br.com.fiap.videosapi.video.presentation.rest.impl;

import br.com.fiap.videosapi.video.application.usecase.VideoDownloadUseCase;
import br.com.fiap.videosapi.video.application.usecase.VideoListUseCase;
import br.com.fiap.videosapi.video.application.usecase.VideoUploadUseCase;
import br.com.fiap.videosapi.video.application.usecase.dto.VideoDownloadData;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoListResponse;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoRestControllerImpl.class)
class VideoRestControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoUploadUseCase videoUploadUseCase;

    @MockBean
    private VideoListUseCase videoListUseCase;

    @MockBean
    private VideoDownloadUseCase videoDownloadUseCase;

    @MockBean
    private AzureBlobStorageService azureBlobStorageService;

    @Test
    @DisplayName("Deve retornar status 400 quando nenhum arquivo for fornecido")
    void deveRetornarBadRequestQuandoNenhumArquivoForFornecido() throws Exception {
        mockMvc.perform(multipart("/api/v1/videos/upload")
                        .file("files", new byte[0])
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("x-cliente-id", "cliente1"))
                .andExpect(status().isBadRequest());

        verify(videoUploadUseCase, never()).uploadVideos(anyList());
    }

    @Test
    @DisplayName("Deve listar todos os vídeos com sucesso")
    void deveListarTodosOsVideosComSucesso() throws Exception {
        List<VideoListResponse> videos = Arrays.asList(
                VideoListResponse.builder().id(1L).originalFileName("video1.mp4").build(),
                VideoListResponse.builder().id(2L).originalFileName("video2.mp4").build()
        );

        when(videoListUseCase.listAllVideos()).thenReturn(videos);

        mockMvc.perform(get("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-cliente-id", "cliente1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(videoListUseCase, times(1)).listAllVideos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver vídeos")
    void deveRetornarListaVaziaQuandoNaoHouverVideos() throws Exception {
        when(videoListUseCase.listAllVideos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-cliente-id", "cliente1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(videoListUseCase, times(1)).listAllVideos();
    }

    @Test
    @DisplayName("Deve listar vídeos por status com sucesso")
    void deveListarVideosPorStatusComSucesso() throws Exception {
        List<VideoListResponse> videos = Arrays.asList(
                VideoListResponse.builder().id(1L).originalFileName("video1.mp4").status(VideoStatus.PROCESSED).build(),
                VideoListResponse.builder().id(2L).originalFileName("video2.mp4").status(VideoStatus.PROCESSED).build()
        );

        when(videoListUseCase.listVideosByStatus(VideoStatus.PROCESSED)).thenReturn(videos);

        mockMvc.perform(get("/api/v1/videos/status/PROCESSED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-cliente-id", "cliente1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[0].status").value("PROCESSED"));

        verify(videoListUseCase, times(1)).listVideosByStatus(VideoStatus.PROCESSED);
    }

    @Test
    @DisplayName("Deve retornar vídeo por ID com sucesso")
    void deveRetornarVideoPorIdComSucesso() throws Exception {
        VideoListResponse video = VideoListResponse.builder()
                .id(1L)
                .originalFileName("video1.mp4")
                .status(VideoStatus.PROCESSED)
                .build();

        when(videoListUseCase.getVideoById(1L)).thenReturn(video);

        mockMvc.perform(get("/api/v1/videos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-cliente-id", "cliente1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originalFileName").value("video1.mp4"));

        verify(videoListUseCase, times(1)).getVideoById(1L);
    }

    @Test
    @DisplayName("Deve retornar status 404 quando vídeo não for encontrado")
    void deveRetornarNotFoundQuandoVideoNaoForEncontrado() throws Exception {
        when(videoListUseCase.getVideoById(999L)).thenThrow(new RuntimeException("Vídeo não encontrado"));

        mockMvc.perform(get("/api/v1/videos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-cliente-id", "cliente1"))
                .andExpect(status().isNotFound());

        verify(videoListUseCase, times(1)).getVideoById(999L);
    }

    @Test
    @DisplayName("Deve retornar URL pública SAS para download do vídeo")
    void deveRetornarUrlPublicaSasParaDownload() throws Exception {
        VideoDownloadData data = VideoDownloadData.builder()
                .video(null)
                .videoBlobName("cliente1/1/frames/frames.zip")
                .zipFileName("frames-video.mp4.zip")
                .build();
        String publicUrl = "https://blob.url/cliente1/1/frames/frames.zip?sv=2023-11-03&sp=r&sig=abcdef";

        when(videoDownloadUseCase.prepareDownload(1L, "cliente1")).thenReturn(data);
        when(azureBlobStorageService.generatePublicUrl("cliente1/1/frames/frames.zip")).thenReturn(publicUrl);

        mockMvc.perform(get("/api/v1/videos/1/download-url")
                        .header("x-cliente-id", "cliente1"))
                .andExpect(status().isOk())
                .andExpect(content().string(publicUrl));
    }


    @Test
    @DisplayName("Deve retornar 500 quando ocorrer erro inesperado no download")
    void deveRetornarInternalServerErrorQuandoErroInesperado() throws Exception {
        when(videoDownloadUseCase.prepareDownload(anyLong(), anyString())).thenThrow(new RuntimeException("Erro inesperado"));

        mockMvc.perform(get("/api/v1/videos/1/download").header("x-cliente-id", "cliente1"))
                .andExpect(status().isInternalServerError());
    }
}
