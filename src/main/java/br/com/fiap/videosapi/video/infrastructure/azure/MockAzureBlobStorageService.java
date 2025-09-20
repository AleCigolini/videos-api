//package br.com.fiap.videosapi.video.infrastructure.azure;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.UUID;
//
//@Service
//@Slf4j
//@ConditionalOnProperty(name = "azure.storage.mock.enabled", havingValue = "true")
//public class MockAzureBlobStorageService extends AzureBlobStorageService {
//
//    private final String localStoragePath;
//    private final String mockBaseUrl;
//
//    public MockAzureBlobStorageService() {
//        super("mock-connection-string", "mock-container");
//        this.localStoragePath = System.getProperty("java.io.tmpdir") + "/mock-azure-storage/";
//        this.mockBaseUrl = "http://localhost:8080/mock-storage/";
//        createLocalStorageDirectory();
//    }
//
//    @Override
//    public AzureBlobUploadResult uploadVideo(MultipartFile file) {
//        try {
//            String fileName = generateUniqueFileName(file.getOriginalFilename());
//            Path filePath = Paths.get(localStoragePath, fileName);
//
//            // Save file locally
//            Files.copy(file.getInputStream(), filePath);
//
//            log.info("Successfully uploaded file {} to mock Azure Blob Storage at {}", fileName, filePath);
//
//            return AzureBlobUploadResult.builder()
//                    .fileName(fileName)
//                    .blobUrl(mockBaseUrl + fileName)
//                    .containerName("mock-videos")
//                    .fileSize(file.getSize())
//                    .contentType(file.getContentType())
//                    .success(true)
//                    .build();
//
//        } catch (IOException e) {
//            log.error("Error uploading file to mock Azure Blob Storage", e);
//            return AzureBlobUploadResult.builder()
//                    .success(false)
//                    .errorMessage("Failed to upload file: " + e.getMessage())
//                    .build();
//        }
//    }
//
//    @Override
//    public boolean deleteVideo(String fileName) {
//        try {
//            Path filePath = Paths.get(localStoragePath, fileName);
//            boolean deleted = Files.deleteIfExists(filePath);
//
//            if (deleted) {
//                log.info("Successfully deleted file {} from mock Azure Blob Storage", fileName);
//            } else {
//                log.warn("File {} not found in mock Azure Blob Storage", fileName);
//            }
//
//            return deleted;
//        } catch (Exception e) {
//            log.error("Error deleting file {} from mock Azure Blob Storage", fileName, e);
//            return false;
//        }
//    }
//
//    private void createLocalStorageDirectory() {
//        try {
//            Path directory = Paths.get(localStoragePath);
//            if (!Files.exists(directory)) {
//                Files.createDirectories(directory);
//                log.info("Created mock storage directory: {}", localStoragePath);
//            }
//        } catch (IOException e) {
//            log.error("Error creating mock storage directory: {}", localStoragePath, e);
//        }
//    }
//
//    private String generateUniqueFileName(String originalFileName) {
//        String extension = "";
//        if (originalFileName != null && originalFileName.contains(".")) {
//            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
//        }
//        return UUID.randomUUID().toString() + extension;
//    }
//}
