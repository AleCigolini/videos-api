package br.com.fiap.videosapi.video.infrastructure.controller;

import br.com.fiap.videosapi.video.application.usecase.VideoListUseCase;
import br.com.fiap.videosapi.video.application.usecase.VideoUploadUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoListResponse;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.presentation.rest.impl.VideoRestControllerImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoRestControllerImpl.class)
class VideoRestControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoUploadUseCase videoUploadUseCase;

    @MockBean
    private VideoListUseCase videoListUseCase;

    @Test
    @DisplayName("Deve retornar status 400 quando nenhum arquivo for fornecido")
    void deveRetornarBadRequestQuandoNenhumArquivoForFornecido() throws Exception {
        mockMvc.perform(multipart("/api/v1/videos/upload")
                        .file("files", new byte[0])
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(videoUploadUseCase, never()).uploadVideos(anyList());
    }

    @Test
    @DisplayName("Deve retornar status 201 quando todos os vídeos forem carregados com sucesso")
    void deveRetornarCreatedQuandoTodosVideosForemCarregados() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "video1.mp4",
                "video/mp4",
                "conteúdo do vídeo 1".getBytes());

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "video2.mp4",
                "video/mp4",
                "conteúdo do vídeo 2".getBytes());

        List<VideoUploadResponse> successResponses = Arrays.asList(
                VideoUploadResponse.builder().id(1L).originalFileName("video1.mp4").message("Upload com sucesso").build(),
                VideoUploadResponse.builder().id(2L).originalFileName("video2.mp4").message("Upload com sucesso").build()
        );

        when(videoUploadUseCase.uploadVideos(anyList())).thenReturn(successResponses);

        mockMvc.perform(multipart("/api/v1/videos/upload")
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(videoUploadUseCase, times(1)).uploadVideos(anyList());
    }

    @Test
    @DisplayName("Deve retornar status 207 quando alguns vídeos falharem no upload")
    void deveRetornarMultiStatusQuandoAlgunsVideosFalharem() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "video1.mp4",
                "video/mp4",
                "conteúdo do vídeo 1".getBytes());

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "video2.mp4",
                "video/mp4",
                "conteúdo do vídeo 2".getBytes());

        List<VideoUploadResponse> mixedResponses = Arrays.asList(
                VideoUploadResponse.builder().id(1L).originalFileName("video1.mp4").message("Upload com sucesso").build(),
                VideoUploadResponse.builder().originalFileName("video2.mp4").message("Falha no upload").build()
        );

        when(videoUploadUseCase.uploadVideos(anyList())).thenReturn(mixedResponses);

        mockMvc.perform(multipart("/api/v1/videos/upload")
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").doesNotExist());

        verify(videoUploadUseCase, times(1)).uploadVideos(anyList());
    }

    @Test
    @DisplayName("Deve retornar status 500 quando ocorrer uma exceção durante o upload")
    void deveRetornarInternalServerErrorQuandoOcorrerExcecao() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "video1.mp4",
                "video/mp4",
                "conteúdo do vídeo 1".getBytes());

        when(videoUploadUseCase.uploadVideos(anyList())).thenThrow(new RuntimeException("Erro no servidor"));

        mockMvc.perform(multipart("/api/v1/videos/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());

        verify(videoUploadUseCase, times(1)).uploadVideos(anyList());
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
                        .contentType(MediaType.APPLICATION_JSON))
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
                        .contentType(MediaType.APPLICATION_JSON))
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
                        .contentType(MediaType.APPLICATION_JSON))
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
                        .contentType(MediaType.APPLICATION_JSON))
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
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(videoListUseCase, times(1)).getVideoById(999L);
    }
}
