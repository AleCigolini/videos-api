package br.com.fiap.videosapi.video.application.usecase;

import br.com.fiap.videosapi.video.common.domain.dto.response.VideoListResponse;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;

import java.util.List;

public interface VideoListUseCase {
    List<VideoListResponse> listAllVideos();
    List<VideoListResponse> listVideosByStatus(VideoStatus status);
    VideoListResponse getVideoById(Long id);
}
