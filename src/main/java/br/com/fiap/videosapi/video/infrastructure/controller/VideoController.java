package br.com.fiap.videosapi.video.infrastructure.controller;

import br.com.fiap.videosapi.video.application.usecase.VideoUploadUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Video Management", description = "API for video upload and management")
public class VideoController {

    private final VideoUploadUseCase videoUploadUseCase;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a video file",
            description = "Upload a video file to Azure Blob Storage and publish upload event to Kafka"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Video uploaded successfully",
                    content = @Content(schema = @Schema(implementation = VideoUploadResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file or request parameters"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during upload"
            )
    })
    public ResponseEntity<VideoUploadResponse> uploadVideo(
            @Parameter(
                    description = "Video file to upload (max 500MB, supported formats: mp4, avi, mov, wmv, flv, webm, mkv)",
                    required = true
            )
            @RequestParam("file") MultipartFile file) {
        
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

    @GetMapping("/health")
    @Operation(
            summary = "Health check endpoint",
            description = "Check if the video service is running"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Video service is running");
    }
}
