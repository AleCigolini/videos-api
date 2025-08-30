package br.com.fiap.videosapi.core.exception;

import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(VideoUploadException.class)
    public ResponseEntity<VideoUploadResponse> handleVideoUploadException(VideoUploadException ex) {
        log.error("Video upload error: {}", ex.getMessage(), ex);
        VideoUploadResponse response = VideoUploadResponse.builder()
                .message("Video upload failed: " + ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(VideoException.class)
    public ResponseEntity<VideoUploadResponse> handleVideoException(VideoException ex) {
        log.error("Video processing error: {}", ex.getMessage(), ex);
        VideoUploadResponse response = VideoUploadResponse.builder()
                .message("Video processing failed: " + ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<VideoUploadResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage(), ex);
        VideoUploadResponse response = VideoUploadResponse.builder()
                .message("Invalid request: " + ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<VideoUploadResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage(), ex);
        VideoUploadResponse response = VideoUploadResponse.builder()
                .message("File size exceeds maximum allowed size of 500MB")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<VideoUploadResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        VideoUploadResponse response = VideoUploadResponse.builder()
                .message("An unexpected error occurred. Please try again later.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
