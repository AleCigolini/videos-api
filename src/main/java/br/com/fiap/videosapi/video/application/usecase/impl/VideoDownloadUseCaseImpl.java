package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.application.usecase.VideoDownloadUseCase;
import br.com.fiap.videosapi.video.application.usecase.dto.VideoDownloadData;
import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobStorageService;
import br.com.fiap.videosapi.video.infrastructure.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoDownloadUseCaseImpl implements VideoDownloadUseCase {

    private final VideoRepository videoRepository;
    private final AzureBlobStorageService azureBlobStorageService;

    @Override
    public VideoDownloadData prepareDownload(Long videoId, String userId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found with id: " + videoId));

        String framesZipBlobName = userId + "/" + videoId + "/frames.zip";
        if (!azureBlobStorageService.blobExists(framesZipBlobName)) {
            throw new IllegalArgumentException("Arquivo frames.zip não encontrado para o vídeo: " + videoId);
        }

        return VideoDownloadData.builder()
                .video(video)
                .videoBlobName(framesZipBlobName)
                .zipFileName("frames-" + video.getOriginalFileName() + ".zip")
                .build();
    }
}
