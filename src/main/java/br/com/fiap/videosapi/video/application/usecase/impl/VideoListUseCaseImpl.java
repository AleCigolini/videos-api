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
import br.com.fiap.videosapi.core.context.UserContext;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoListUseCaseImpl implements VideoListUseCase {

    private final VideoRepository videoRepository;

    @Override
    @Cacheable(value = "videos", key = "'all:' + T(br.com.fiap.videosapi.core.context.UserContext).getUserId()")
    public List<VideoListResponse> listAllVideos() {
        String userId = UserContext.getUserId();
        log.info("Fetching all videos from database for userId={}", userId);
        List<Video> videos = videoRepository.findAllByUserId(userId);
        return videos.stream()
                .map(this::mapToVideoListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "videos", key = "'status_' + #status.name() + ':' + T(br.com.fiap.videosapi.core.context.UserContext).getUserId()")
    public List<VideoListResponse> listVideosByStatus(VideoStatus status) {
        String userId = UserContext.getUserId();
        log.info("Fetching videos with status: {} for userId={}", status, userId);
        List<Video> videos = videoRepository.findByStatusAndUserId(status, userId);
        return videos.stream()
                .map(this::mapToVideoListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "video", key = "#id + ':' + T(br.com.fiap.videosapi.core.context.UserContext).getUserId()")
    public VideoListResponse getVideoById(Long id) {
        String userId = UserContext.getUserId();
        log.info("Fetching video with ID: {} for userId={}", id, userId);
        Video video = videoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + id));
        return mapToVideoListResponse(video);
    }

    private VideoListResponse mapToVideoListResponse(Video video) {
        String downloadUrl = null;
        
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
