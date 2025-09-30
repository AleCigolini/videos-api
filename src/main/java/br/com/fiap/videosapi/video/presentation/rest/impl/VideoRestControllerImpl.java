package br.com.fiap.videosapi.video.presentation.rest.impl;

import br.com.fiap.videosapi.video.application.usecase.VideoDownloadUseCase;
import br.com.fiap.videosapi.video.application.usecase.VideoListUseCase;
import br.com.fiap.videosapi.video.application.usecase.VideoUploadUseCase;
import br.com.fiap.videosapi.video.application.usecase.dto.VideoDownloadData;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoListResponse;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobStorageService;
import br.com.fiap.videosapi.video.presentation.rest.VideoRestController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
@Slf4j
public class VideoRestControllerImpl implements VideoRestController {

    private final VideoUploadUseCase videoUploadUseCase;
    private final VideoListUseCase videoListUseCase;
    private final VideoDownloadUseCase videoDownloadUseCase;
    private final AzureBlobStorageService azureBlobStorageService;

    @Override
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideos(@RequestPart("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            log.warn("No files provided in the upload request");
            return ResponseEntity.badRequest().body("No files provided for upload");
        }

        log.info("Received request to upload {} video(s)", files.size());

        try {
            List<VideoUploadResponse> responses = videoUploadUseCase.uploadVideos(files);

            boolean allSuccessful = responses.stream()
                    .allMatch(response -> response.getId() != null);

            if (allSuccessful) {
                log.info("Successfully uploaded {} videos", responses.size());
                return ResponseEntity.status(HttpStatus.CREATED).body(responses);
            } else {
                log.warn("Some videos failed to upload. Success: {}, Failed: {}",
                        responses.stream().filter(r -> r.getId() != null).count(),
                        responses.stream().filter(r -> r.getId() == null).count());
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responses);
            }

        } catch (Exception e) {
            log.error("Unexpected error during video uploads", e);
            VideoUploadResponse errorResponse = VideoUploadResponse.builder()
                    .message("Unexpected error occurred during video uploads: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    @GetMapping
    public ResponseEntity<List<VideoListResponse>> listAllVideos() {
        log.info("Received request to list all videos");
        List<VideoListResponse> videos = videoListUseCase.listAllVideos();
        return ResponseEntity.ok(videos);
    }

    @Override
    @GetMapping("/status/{status}")
    public ResponseEntity<List<VideoListResponse>> listVideosByStatus(@PathVariable VideoStatus status) {
        log.info("Received request to list videos with status: {}", status);
        List<VideoListResponse> videos = videoListUseCase.listVideosByStatus(status);
        return ResponseEntity.ok(videos);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<VideoListResponse> getVideoById(@PathVariable Long id) {
        log.info("Received request to get video with ID: {}", id);
        try {
            VideoListResponse video = videoListUseCase.getVideoById(id);
            return ResponseEntity.ok(video);
        } catch (RuntimeException e) {
            log.error("Video not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @GetMapping(value = "/{id}/download-url")
    public ResponseEntity<String> downloadCompactedVideoUrl(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        log.info("Received request to generate public download URL for video {}", id);
        try {
            String userId = request.getHeader("x-cliente-id");
            VideoDownloadData data = videoDownloadUseCase.prepareDownload(id, userId);

            String publicUrl = azureBlobStorageService.generatePublicUrl(data.getVideoBlobName());

            return ResponseEntity.ok(publicUrl);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Download URL request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Unexpected error generating public URL for video {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
