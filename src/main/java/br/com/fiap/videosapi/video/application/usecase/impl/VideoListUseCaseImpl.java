package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.application.usecase.VideoListUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.response.VideoListResponse;
import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.infrastructure.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoListUseCaseImpl implements VideoListUseCase {

    private final VideoRepository videoRepository;

    @Override
    @Cacheable(value = "videos", key = "'all'")
    public List<VideoListResponse> listAllVideos() {
        log.info("Fetching all videos from database");
        List<Video> videos = videoRepository.findAll();
        return videos.stream()
                .map(this::mapToVideoListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "videos", key = "'status_' + #status.name()")
    public List<VideoListResponse> listVideosByStatus(VideoStatus status) {
        log.info("Fetching videos with status: {}", status);
        List<Video> videos = videoRepository.findByStatus(status);
        return videos.stream()
                .map(this::mapToVideoListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "video", key = "#id")
    public VideoListResponse getVideoById(Long id) {
        log.info("Fetching video with ID: {}", id);
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + id));
        return mapToVideoListResponse(video);
    }

    private VideoListResponse mapToVideoListResponse(Video video) {
        String downloadUrl = null;
        
        // Generate download URL only for processed videos
        if (video.getStatus() == VideoStatus.PROCESSED) {
            downloadUrl = video.getAzureBlobUrl();
        }

        return VideoListResponse.builder()
                .id(video.getId())
                .originalFileName(video.getOriginalFileName())
                .status(video.getStatus())
                .fileSize(video.getFileSize())
                .downloadUrl(downloadUrl)
                .uploadedAt(video.getUploadedAt())
                .processedAt(video.getProcessedAt())
                .build();
    }
}
