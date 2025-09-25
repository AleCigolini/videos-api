package br.com.fiap.videosapi.video.infrastructure.azure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AzureBlobUploadResultTest {

    @Test
    @DisplayName("Deve criar instância usando builder com sucesso")
    void deveCriarInstanciaUsandoBuilderComSucesso() {
        String fileName = "video-teste.mp4";
        String blobUrl = "https://storage.blob.core.windows.net/videos/video-teste.mp4";
        String containerName = "videos-container";
        Long fileSize = 1024L;
        String contentType = "video/mp4";
        String errorMessage = "Upload falhou";

        AzureBlobUploadResult resultado = AzureBlobUploadResult.builder()
                .fileName(fileName)
                .blobUrl(blobUrl)
                .containerName(containerName)
                .fileSize(fileSize)
                .contentType(contentType)
                .success(true)
                .errorMessage(errorMessage)
                .build();

        assertEquals(fileName, resultado.getFileName());
        assertEquals(blobUrl, resultado.getBlobUrl());
        assertEquals(containerName, resultado.getContainerName());
        assertEquals(fileSize, resultado.getFileSize());
        assertEquals(contentType, resultado.getContentType());
        assertTrue(resultado.isSuccess());
        assertEquals(errorMessage, resultado.getErrorMessage());
    }

    @Test
    @DisplayName("Deve criar instância usando construtor sem argumentos")
    void deveCriarInstanciaUsandoConstrutorSemArgumentos() {
        AzureBlobUploadResult resultado = new AzureBlobUploadResult();

        assertNull(resultado.getFileName());
        assertNull(resultado.getBlobUrl());
        assertNull(resultado.getContainerName());
        assertNull(resultado.getFileSize());
        assertNull(resultado.getContentType());
        assertFalse(resultado.isSuccess());
        assertNull(resultado.getErrorMessage());
    }

    @Test
    @DisplayName("Deve criar instância usando construtor com todos argumentos")
    void deveCriarInstanciaUsandoConstrutorComTodosArgumentos() {
        String fileName = "video-completo.mp4";
        String blobUrl = "https://test.com/video-completo.mp4";
        String containerName = "test-container";
        Long fileSize = 2048L;
        String contentType = "video/avi";
        boolean success = false;
        String errorMessage = "Erro no processamento";

        AzureBlobUploadResult resultado = new AzureBlobUploadResult(
                fileName, blobUrl, containerName, fileSize, contentType, success, errorMessage
        );

        assertEquals(fileName, resultado.getFileName());
        assertEquals(blobUrl, resultado.getBlobUrl());
        assertEquals(containerName, resultado.getContainerName());
        assertEquals(fileSize, resultado.getFileSize());
        assertEquals(contentType, resultado.getContentType());
        assertEquals(success, resultado.isSuccess());
        assertEquals(errorMessage, resultado.getErrorMessage());
    }

    @Test
    @DisplayName("Deve modificar valores usando setters")
    void deveModificarValoresUsandoSetters() {
        AzureBlobUploadResult resultado = new AzureBlobUploadResult();

        String novoFileName = "novo-video.mp4";
        String novoBlobUrl = "https://novo.com/video.mp4";
        String novoContainer = "novo-container";
        Long novoFileSize = 4096L;
        String novoContentType = "video/mov";
        String novoErrorMessage = "Novo erro";

        resultado.setFileName(novoFileName);
        resultado.setBlobUrl(novoBlobUrl);
        resultado.setContainerName(novoContainer);
        resultado.setFileSize(novoFileSize);
        resultado.setContentType(novoContentType);
        resultado.setSuccess(true);
        resultado.setErrorMessage(novoErrorMessage);

        assertEquals(novoFileName, resultado.getFileName());
        assertEquals(novoBlobUrl, resultado.getBlobUrl());
        assertEquals(novoContainer, resultado.getContainerName());
        assertEquals(novoFileSize, resultado.getFileSize());
        assertEquals(novoContentType, resultado.getContentType());
        assertTrue(resultado.isSuccess());
        assertEquals(novoErrorMessage, resultado.getErrorMessage());
    }

    @Test
    @DisplayName("Deve retornar true para equals com objetos iguais")
    void deveRetornarTrueParaEqualsComObjetosIguais() {
        AzureBlobUploadResult resultado1 = AzureBlobUploadResult.builder()
                .fileName("video.mp4")
                .blobUrl("https://test.com/video.mp4")
                .containerName("container")
                .fileSize(1024L)
                .contentType("video/mp4")
                .success(true)
                .errorMessage("Mensagem")
                .build();

        AzureBlobUploadResult resultado2 = AzureBlobUploadResult.builder()
                .fileName("video.mp4")
                .blobUrl("https://test.com/video.mp4")
                .containerName("container")
                .fileSize(1024L)
                .contentType("video/mp4")
                .success(true)
                .errorMessage("Mensagem")
                .build();

        assertEquals(resultado1, resultado2);
        assertEquals(resultado1.hashCode(), resultado2.hashCode());
    }

    @Test
    @DisplayName("Deve retornar false para equals com objetos diferentes")
    void deveRetornarFalseParaEqualsComObjetosDiferentes() {
        AzureBlobUploadResult resultado1 = AzureBlobUploadResult.builder()
                .fileName("video1.mp4")
                .success(true)
                .build();

        AzureBlobUploadResult resultado2 = AzureBlobUploadResult.builder()
                .fileName("video2.mp4")
                .success(false)
                .build();

        assertNotEquals(resultado1, resultado2);
        assertNotEquals(resultado1.hashCode(), resultado2.hashCode());
    }

    @Test
    @DisplayName("Deve retornar false para equals com null")
    void deveRetornarFalseParaEqualsComNull() {
        AzureBlobUploadResult resultado = AzureBlobUploadResult.builder().build();

        assertNotEquals(resultado, null);
    }

    @Test
    @DisplayName("Deve retornar true para equals com mesmo objeto")
    void deveRetornarTrueParaEqualsComMesmoObjeto() {
        AzureBlobUploadResult resultado = AzureBlobUploadResult.builder().build();

        assertEquals(resultado, resultado);
    }

    @Test
    @DisplayName("Deve gerar toString adequado")
    void deveGerarToStringAdequado() {
        AzureBlobUploadResult resultado = AzureBlobUploadResult.builder()
                .fileName("test.mp4")
                .blobUrl("https://test.com/test.mp4")
                .success(true)
                .build();

        String toStringResult = resultado.toString();

        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("AzureBlobUploadResult"));
        assertTrue(toStringResult.contains("test.mp4"));
        assertTrue(toStringResult.contains("https://test.com/test.mp4"));
        assertTrue(toStringResult.contains("success=true"));
    }

    @Test
    @DisplayName("Deve criar resultado de sucesso com dados completos")
    void deveCriarResultadoDeSucessoComDadosCompletos() {
        AzureBlobUploadResult resultado = AzureBlobUploadResult.builder()
                .fileName("upload-sucesso.mp4")
                .blobUrl("https://storage.com/upload-sucesso.mp4")
                .containerName("videos")
                .fileSize(5120L)
                .contentType("video/mp4")
                .success(true)
                .build();

        assertTrue(resultado.isSuccess());
        assertNotNull(resultado.getFileName());
        assertNotNull(resultado.getBlobUrl());
        assertNotNull(resultado.getContainerName());
        assertNotNull(resultado.getFileSize());
        assertNotNull(resultado.getContentType());
        assertNull(resultado.getErrorMessage());
    }

    @Test
    @DisplayName("Deve criar resultado de erro com mensagem")
    void deveCriarResultadoDeErroComMensagem() {
        AzureBlobUploadResult resultado = AzureBlobUploadResult.builder()
                .fileName("upload-falhou.mp4")
                .success(false)
                .errorMessage("Falha na conexão com Azure")
                .build();

        assertFalse(resultado.isSuccess());
        assertEquals("upload-falhou.mp4", resultado.getFileName());
        assertEquals("Falha na conexão com Azure", resultado.getErrorMessage());
        assertNull(resultado.getBlobUrl());
        assertNull(resultado.getFileSize());
    }

    @Test
    @DisplayName("Deve permitir fileSize nulo")
    void devePermitirFileSizeNulo() {
        AzureBlobUploadResult resultado = AzureBlobUploadResult.builder()
                .fileName("video.mp4")
                .fileSize(null)
                .build();

        assertNull(resultado.getFileSize());
        assertEquals("video.mp4", resultado.getFileName());
    }

    @Test
    @DisplayName("Deve permitir todos os campos nulos")
    void devePermitirTodosOsCamposNulos() {
        AzureBlobUploadResult resultado = AzureBlobUploadResult.builder()
                .fileName(null)
                .blobUrl(null)
                .containerName(null)
                .fileSize(null)
                .contentType(null)
                .success(false)
                .errorMessage(null)
                .build();

        assertNull(resultado.getFileName());
        assertNull(resultado.getBlobUrl());
        assertNull(resultado.getContainerName());
        assertNull(resultado.getFileSize());
        assertNull(resultado.getContentType());
        assertFalse(resultado.isSuccess());
        assertNull(resultado.getErrorMessage());
    }
}
