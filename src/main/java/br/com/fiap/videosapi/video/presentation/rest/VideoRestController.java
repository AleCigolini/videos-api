package br.com.fiap.videosapi.video.presentation.rest;

import br.com.fiap.videosapi.video.common.domain.dto.response.VideoListResponse;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "${tag.swagger.video.name}", description = "${tag.swagger.video.description}")
public interface VideoRestController {

    @Operation(
            summary = "Upload multiple video files",
            description = "Upload multiple video files to Azure Blob Storage and publish upload events to Kafka"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "All videos uploaded successfully",
                    content = @Content(schema = @Schema(implementation = VideoUploadResponse.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "207",
                    description = "Some videos failed to upload",
                    content = @Content(schema = @Schema(implementation = VideoUploadResponse.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No files provided or invalid request parameters"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during upload"
            )
    })
    ResponseEntity<?> uploadVideos(
            @Parameter(description = "Video files to upload (max 500MB each, supported formats: mp4, avi, mov, wmv, flv, webm, mkv)", required = true)
            @RequestParam("files") List<MultipartFile> files);

    @Operation(
            summary = "List all videos",
            description = "Retrieve a list of all uploaded videos with their processing status"
    )
    @ApiResponse(responseCode = "200", description = "Videos retrieved successfully")
    ResponseEntity<List<VideoListResponse>> listAllVideos();

    @Operation(
            summary = "List videos by status",
            description = "Retrieve videos filtered by processing status"
    )
    @ApiResponse(responseCode = "200", description = "Videos retrieved successfully")
    ResponseEntity<List<VideoListResponse>> listVideosByStatus(
            @Parameter(description = "Video processing status", required = true)
            @PathVariable VideoStatus status);


    @Operation(
            summary = "Get video by ID",
            description = "Retrieve a specific video by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video found"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    ResponseEntity<VideoListResponse> getVideoById(@Parameter(description = "Video ID", required = true) @PathVariable Long id);
}
