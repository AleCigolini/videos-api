package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.application.usecase.VideoUploadUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.event.VideoUploadEvent;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobStorageService;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobUploadResult;
import br.com.fiap.videosapi.video.infrastructure.kafka.VideoEventProducer;
import br.com.fiap.videosapi.video.infrastructure.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoUploadUseCaseImpl implements VideoUploadUseCase {

    private final AzureBlobStorageService azureBlobStorageService;
    private final VideoEventProducer videoEventProducer;
    private final VideoRepository videoRepository;
    private final Tika tika = new Tika();

    @Value("${azure.storage.connection-string}")
    String connectionString;

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/avi", "video/mov", "video/wmv", "video/flv", "video/webm", "video/mkv"
    );

    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB

    @Override
    @Transactional
    public List<VideoUploadResponse> uploadVideos(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for upload");
        }

        return files.stream()
                .map(this::uploadVideo)
                .toList();
    }

    @Override
    @Transactional
    public VideoUploadResponse uploadVideo(MultipartFile file) {
        try {
            // Validate file
            validateVideoFile(file);

            // Upload to Azure Blob Storage
            AzureBlobUploadResult uploadResult = azureBlobStorageService.uploadVideo(file);

            if (!uploadResult.isSuccess()) {
                throw new RuntimeException("Failed to upload video to Azure: " + uploadResult.getErrorMessage());
            }

            // Save video metadata to database
            Video video = createVideoFromUpload(file, uploadResult);
            video = videoRepository.save(video);

            // Publish event for video processing
            VideoUploadEvent event = createVideoUploadEvent(video, uploadResult);
            videoEventProducer.publishVideoUploadEvent(event);

            return buildSuccessResponse(video);

        } catch (Exception e) {
            log.error("Error uploading video: {}", e.getMessage(), e);
            return VideoUploadResponse.builder()
                    .message("Error uploading video: " + e.getMessage())
                    .build();
        }
    }

    private Video createVideoFromUpload(MultipartFile file, AzureBlobUploadResult uploadResult) {
        return Video.builder()
                .originalFileName(file.getOriginalFilename())
                .storedFileName(uploadResult.getFileName())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .azureBlobUrl(uploadResult.getBlobUrl())
                .containerName(uploadResult.getContainerName())
                .status(VideoStatus.UPLOADED)
                .build();
    }

    private VideoUploadEvent createVideoUploadEvent(Video video, AzureBlobUploadResult uploadResult) {
        return VideoUploadEvent.builder()
                .videoId(video.getId())
                .azureBlobUrl(uploadResult.getBlobUrl())
                .originalFileName(video.getOriginalFileName())
                .storedFileName(video.getStoredFileName())
                .contentType(video.getContentType())
                .fileSize(video.getFileSize())
                .containerName(video.getContainerName())
                .connectionString(connectionString)
                .status(video.getStatus())
                .uploadedAt(video.getUploadedAt())
                .eventType("VIDEO_UPLOAD_SUCCESS")
                .build();
    }

    private VideoUploadResponse buildSuccessResponse(Video video) {
        return VideoUploadResponse.builder()
                .id(video.getId())
                .originalFileName(video.getOriginalFileName())
                .storedFileName(video.getStoredFileName())
                .contentType(video.getContentType())
                .fileSize(video.getFileSize())
                .azureBlobUrl(video.getAzureBlobUrl())
                .status(video.getStatus())
                .uploadedAt(video.getUploadedAt())
                .message("Video uploaded successfully and queued for processing")
                .build();
    }

    private void validateVideoFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 500MB");
        }

        // Detect MIME type using Apache Tika for better accuracy
        String detectedMimeType = tika.detect(file.getInputStream());

        if (detectedMimeType == null || !ALLOWED_VIDEO_TYPES.contains(detectedMimeType)) {
            throw new IllegalArgumentException("Invalid file type. Only video files are allowed. Detected type: " + detectedMimeType);
        }

        log.info("Video file validation passed for file: {} with type: {}", file.getOriginalFilename(), detectedMimeType);
    }
}
