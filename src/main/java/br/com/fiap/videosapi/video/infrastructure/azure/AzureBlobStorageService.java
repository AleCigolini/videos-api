package br.com.fiap.videosapi.video.infrastructure.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.sas.BlobSasPermission;
import java.time.OffsetDateTime;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class AzureBlobStorageService {

    private final BlobServiceClient blobServiceClient;
    private final String containerName;

    public AzureBlobStorageService(
            @Value("${azure.storage.container-name}") String containerName,
            BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
        this.containerName = containerName;
    }

    public AzureBlobUploadResult uploadVideo(MultipartFile file, Long idVideo) {
        createContainerIfNotExists();
        try {
            String originalFileName = file.getOriginalFilename();
            String fileName = idVideo + "/" + originalFileName;
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

    public boolean blobExists(String blobName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        return containerClient.getBlobClient(blobName).exists();
    }

    public String generatePublicUrl(String blobName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusHours(1),
                BlobSasPermission.parse("r")
        );
        String sasToken = blobClient.generateSas(values);
        return blobClient.getBlobUrl() + "?" + sasToken;
    }

    private void createContainerIfNotExists() {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (containerClient != null && !containerClient.exists()) {
                containerClient.create();
                log.info("Created container: {}", containerName);
            }
        } catch (Exception e) {
            log.error("Error creating container: {}", containerName, e);
        }
    }
}
