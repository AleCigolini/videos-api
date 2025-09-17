package br.com.fiap.videosapi.video.infrastructure.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
public class AzureBlobStorageService {

    private final BlobServiceClient blobServiceClient;
    private final String containerName;

    public AzureBlobStorageService(@Value("${azure.storage.connection-string}") String connectionString,
                                   @Value("${azure.storage.container-name}") String containerName) {
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.containerName = containerName;
        createContainerIfNotExists();
    }

    public AzureBlobUploadResult uploadVideo(MultipartFile file) {
        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(fileName);

            try (InputStream inputStream = file.getInputStream()) {
                blobClient.upload(inputStream, file.getSize(), true);
                log.info("Successfully uploaded file {} to Azure Blob Storage", fileName);
                
                return AzureBlobUploadResult.builder()
                        .fileName(fileName)
                        .blobUrl(blobClient.getBlobUrl())
                        .containerName(containerName)
                        .fileSize(file.getSize())
                        .contentType(file.getContentType())
                        .success(true)
                        .build();
            }
        } catch (IOException e) {
            log.error("Error uploading file to Azure Blob Storage", e);
            return AzureBlobUploadResult.builder()
                    .success(false)
                    .errorMessage("Failed to upload file: " + e.getMessage())
                    .build();
        }
    }

    public boolean deleteVideo(String fileName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.delete();
            log.info("Successfully deleted file {} from Azure Blob Storage", fileName);
            return true;
        } catch (Exception e) {
            log.error("Error deleting file {} from Azure Blob Storage", fileName, e);
            return false;
        }
    }

    private void createContainerIfNotExists() {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
                log.info("Created container: {}", containerName);
            }
        } catch (Exception e) {
            log.error("Error creating container: {}", containerName, e);
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }
}
