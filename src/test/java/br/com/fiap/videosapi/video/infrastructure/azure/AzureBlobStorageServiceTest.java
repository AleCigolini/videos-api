package br.com.fiap.videosapi.video.infrastructure.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.specialized.BlobInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AzureBlobStorageServiceTest {

    @Mock
    private BlobServiceClient blobServiceClient;
    @Mock
    private BlobContainerClient blobContainerClient;
    @Mock
    private BlobClient blobClient;
    @Mock
    private MultipartFile multipartFile;

    private AzureBlobStorageService azureBlobStorageService;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        when(blobServiceClient.getBlobContainerClient(anyString())).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        azureBlobStorageService = new AzureBlobStorageService("teste", blobServiceClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("Deve fazer upload de vídeo com sucesso")
    void deveFazerUploadComSucesso() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("video.mp4");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("conteudo".getBytes()));
        when(multipartFile.getSize()).thenReturn(7L);
        when(multipartFile.getContentType()).thenReturn("video/mp4");
        when(blobClient.getBlobUrl()).thenReturn("https://blob.url/video.mp4");
        when(blobContainerClient.exists()).thenReturn(true);

        AzureBlobUploadResult result = azureBlobStorageService.uploadVideo(multipartFile, 1L);

        assertTrue(result.isSuccess());
        assertEquals("1/video.mp4", result.getFileName());
        assertEquals("https://blob.url/video.mp4", result.getBlobUrl());
        assertEquals("teste", result.getContainerName());
        assertEquals(7L, result.getFileSize());
        assertEquals("video/mp4", result.getContentType());
        verify(blobClient).upload(any(InputStream.class), eq(7L), eq(true));
    }

    @Test
    @DisplayName("Deve retornar erro ao fazer upload quando ocorre IOException")
    void deveRetornarErroNoUploadQuandoIOException() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("video.mp4");
        when(multipartFile.getInputStream()).thenThrow(new IOException("erro de IO"));

        AzureBlobUploadResult result = azureBlobStorageService.uploadVideo(multipartFile, 1L);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("erro de IO"));
    }

    @Test
    @DisplayName("Deve abrir InputStream do blob corretamente")
    void deveAbrirInputStreamDoBlob() {
        BlobInputStream fakeStream = mock(BlobInputStream.class);
        when(blobClient.openInputStream()).thenReturn(fakeStream);
        when(blobClient.exists()).thenReturn(true);

        InputStream result = azureBlobStorageService.openBlobInputStream("blob.mp4");
        assertNotNull(result);
        assertEquals(fakeStream, result);
        verify(blobContainerClient).getBlobClient("blob.mp4");
        verify(blobClient).openInputStream();
    }
}
