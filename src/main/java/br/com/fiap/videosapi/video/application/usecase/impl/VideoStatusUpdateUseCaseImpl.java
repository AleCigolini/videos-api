package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.application.usecase.VideoStatusUpdateUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.event.VideoStatusUpdateEvent;
import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import br.com.fiap.videosapi.video.infrastructure.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoStatusUpdateUseCaseImpl implements VideoStatusUpdateUseCase {

    private final VideoRepository videoRepository;
//    private final VideoCacheService videoCacheService;

    @Override
    @Transactional
    @CacheEvict(value = {"videos", "video"}, allEntries = true)
    public void updateVideoStatus(Long videoId, VideoStatus newStatus, String message, String processedBy) {
        log.info("Updating video status for ID: {} to status: {}", videoId, newStatus);
        
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + videoId));

        VideoStatus previousStatus = video.getStatus();
        video.setStatus(newStatus);
        
        if (newStatus == VideoStatus.PROCESSED) {
            video.setProcessedAt(LocalDateTime.now());
        }
        
        videoRepository.save(video);
        
        // Update Redis cache for quick status lookup
//        videoCacheService.updateVideoStatusCache(videoId, newStatus);
//        videoCacheService.cacheVideo(video);
        
        log.info("Video status updated successfully for ID: {} from {} to {}", 
                videoId, previousStatus, newStatus);
    }

    @Override
    @Transactional
    public void processStatusUpdateEvent(VideoStatusUpdateEvent event) {
        log.info("Processing status update event for video ID: {}", event.getVideoId());
        
        try {
            if (event.getUserId() == null || event.getUserId().isBlank()) {
                throw new IllegalArgumentException("Missing userId in VideoStatusUpdateEvent");
            }

            Optional<Video> videoOpt = videoRepository.findById(event.getVideoId());
            if (videoOpt.isEmpty()) {
                throw new RuntimeException("Video not found with ID: " + event.getVideoId());
            }

            Video video = videoOpt.get();
            if (!event.getUserId().equals(video.getUserId())) {
                throw new IllegalArgumentException("User mismatch for video ID: " + event.getVideoId());
            }

            updateVideoStatus(
                    event.getVideoId(),
                    event.getNewStatus(),
                    event.getMessage(),
                    event.getProcessedBy()
            );
            
            log.info("Status update event processed successfully for video ID: {}", event.getVideoId());
        } catch (Exception e) {
            log.error("Error processing status update event for video ID: {}", 
                    event.getVideoId(), e);
            throw e;
        }
    }
}
