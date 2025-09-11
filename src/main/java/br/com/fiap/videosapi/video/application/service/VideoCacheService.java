package br.com.fiap.videosapi.video.application.service;

import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String VIDEO_KEY_PREFIX = "video:";
    private static final String VIDEO_STATUS_KEY_PREFIX = "video:status:";
    private static final long DEFAULT_TTL_MINUTES = 10;

    public void cacheVideo(Video video) {
        try {
            String key = VIDEO_KEY_PREFIX + video.getId();
            redisTemplate.opsForValue().set(key, video, DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
            
            // Also cache just the status for quick lookups
            String statusKey = VIDEO_STATUS_KEY_PREFIX + video.getId();
            redisTemplate.opsForValue().set(statusKey, video.getStatus().name(), DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
            
            log.debug("Cached video with ID: {} and status: {}", video.getId(), video.getStatus());
        } catch (Exception e) {
            log.error("Error caching video with ID: {}", video.getId(), e);
        }
    }

    public Video getCachedVideo(Long videoId) {
        try {
            String key = VIDEO_KEY_PREFIX + videoId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof Video) {
                log.debug("Retrieved cached video with ID: {}", videoId);
                return (Video) cached;
            }
        } catch (Exception e) {
            log.error("Error retrieving cached video with ID: {}", videoId, e);
        }
        return null;
    }

    public VideoStatus getCachedVideoStatus(Long videoId) {
        try {
            String statusKey = VIDEO_STATUS_KEY_PREFIX + videoId;
            Object cached = redisTemplate.opsForValue().get(statusKey);
            if (cached instanceof String) {
                VideoStatus status = VideoStatus.valueOf((String) cached);
                log.debug("Retrieved cached status for video ID: {} - {}", videoId, status);
                return status;
            }
        } catch (Exception e) {
            log.error("Error retrieving cached status for video ID: {}", videoId, e);
        }
        return null;
    }

    public void evictVideoCache(Long videoId) {
        try {
            String key = VIDEO_KEY_PREFIX + videoId;
            String statusKey = VIDEO_STATUS_KEY_PREFIX + videoId;
            
            redisTemplate.delete(key);
            redisTemplate.delete(statusKey);
            
            log.debug("Evicted cache for video ID: {}", videoId);
        } catch (Exception e) {
            log.error("Error evicting cache for video ID: {}", videoId, e);
        }
    }

    public void updateVideoStatusCache(Long videoId, VideoStatus newStatus) {
        try {
            String statusKey = VIDEO_STATUS_KEY_PREFIX + videoId;
            redisTemplate.opsForValue().set(statusKey, newStatus.name(), DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
            
            log.debug("Updated cached status for video ID: {} to {}", videoId, newStatus);
        } catch (Exception e) {
            log.error("Error updating cached status for video ID: {}", videoId, e);
        }
    }
}
