package br.com.fiap.videosapi.video.application.usecase;

import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoUploadUseCase {
    List<VideoUploadResponse> uploadVideos(List<MultipartFile> files);
}
