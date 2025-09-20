package br.com.fiap.videosapi.video.presentation.rest.impl;

import br.com.fiap.videosapi.video.application.usecase.VideoListUseCase;
import br.com.fiap.videosapi.video.application.usecase.VideoUploadUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoListResponse;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.presentation.rest.VideoRestController;
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

    @Override
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoUploadResponse> uploadVideo(@RequestParam("file") MultipartFile file) {

        log.info("Received video upload request for file: {}", file.getOriginalFilename());

        try {
            VideoUploadResponse response = videoUploadUseCase.uploadVideo(file);

            if (response.getId() != null) {
                log.info("Video uploaded successfully with ID: {}", response.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.error("Video upload failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            log.error("Unexpected error during video upload", e);
            VideoUploadResponse errorResponse = VideoUploadResponse.builder()
                    .message("Unexpected error occurred during video upload: " + e.getMessage())
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
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Video service is running");
    }
}
