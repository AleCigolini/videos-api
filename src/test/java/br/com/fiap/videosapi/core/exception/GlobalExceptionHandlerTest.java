package br.com.fiap.videosapi.core.exception;

import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    @DisplayName("Deve tratar VideoUploadException e retornar BAD_REQUEST")
    void deveTratarVideoUploadExceptionERetornarBadRequest() {
        VideoUploadException exception = new VideoUploadException("Erro no upload do vídeo");

        ResponseEntity<VideoUploadResponse> response = globalExceptionHandler.handleVideoUploadException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Video upload failed: Erro no upload do vídeo", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve tratar VideoException e retornar INTERNAL_SERVER_ERROR")
    void deveTratarVideoExceptionERetornarInternalServerError() {
        VideoException exception = new VideoException("Erro no processamento do vídeo");

        ResponseEntity<VideoUploadResponse> response = globalExceptionHandler.handleVideoException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Video processing failed: Erro no processamento do vídeo", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException e retornar BAD_REQUEST")
    void deveTratarIllegalArgumentExceptionERetornarBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        ResponseEntity<VideoUploadResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid request: Argumento inválido", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve tratar MaxUploadSizeExceededException e retornar BAD_REQUEST")
    void deveTratarMaxUploadSizeExceededExceptionERetornarBadRequest() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(500 * 1024 * 1024);

        ResponseEntity<VideoUploadResponse> response = globalExceptionHandler.handleMaxUploadSizeExceededException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("File size exceeds maximum allowed size of 500MB", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve tratar Exception genérica e retornar INTERNAL_SERVER_ERROR")
    void deveTratarExceptionGenericaERetornarInternalServerError() {
        Exception exception = new Exception("Erro inesperado");

        ResponseEntity<VideoUploadResponse> response = globalExceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve criar response com status correto para VideoUploadException")
    void deveCriarResponseComStatusCorretoParaVideoUploadException() {
        VideoUploadException exception = new VideoUploadException("Teste de erro");

        ResponseEntity<VideoUploadResponse> response = globalExceptionHandler.handleVideoUploadException(exception);

        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Video upload failed"));
    }

    @Test
    @DisplayName("Deve criar response com status correto para VideoException")
    void deveCriarResponseComStatusCorretoParaVideoException() {
        VideoException exception = new VideoException("Teste de erro de processamento");

        ResponseEntity<VideoUploadResponse> response = globalExceptionHandler.handleVideoException(exception);

        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Video processing failed"));
    }

    @Test
    @DisplayName("Deve manter mensagem de erro original na resposta")
    void deveManterMensagemDeErroOriginalNaResposta() {
        String mensagemOriginal = "Arquivo muito grande para upload";
        VideoUploadException exception = new VideoUploadException(mensagemOriginal);

        ResponseEntity<VideoUploadResponse> response = globalExceptionHandler.handleVideoUploadException(exception);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains(mensagemOriginal));
    }

    @Test
    @DisplayName("Deve retornar mensagem padronizada para Exception genérica")
    void deveRetornarMensagemPadronizadaParaExceptionGenerica() {
        Exception exception = new Exception("Qualquer erro");

        ResponseEntity<VideoUploadResponse> response = globalExceptionHandler.handleGenericException(exception);

        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve retornar mensagem específica para MaxUploadSizeExceededException")
    void deveRetornarMensagemEspecificaParaMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(1000);

        ResponseEntity<VideoUploadResponse> response = globalExceptionHandler.handleMaxUploadSizeExceededException(exception);

        assertNotNull(response.getBody());
        assertEquals("File size exceeds maximum allowed size of 500MB", response.getBody().getMessage());
    }
}
