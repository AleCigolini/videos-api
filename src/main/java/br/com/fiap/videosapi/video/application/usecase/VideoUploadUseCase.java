package br.com.fiap.videosapi.video.application.usecase;

import br.com.fiap.videosapi.video.common.domain.dto.response.VideoUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VideoUploadUseCase {
    VideoUploadResponse uploadVideo(MultipartFile file);
}
