package br.com.fiap.videosapi.video.application.usecase.impl;

import br.com.fiap.videosapi.video.application.usecase.VideoDownloadUseCase;
import br.com.fiap.videosapi.video.application.usecase.dto.VideoDownloadData;
import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.infrastructure.azure.AzureBlobStorageService;
import br.com.fiap.videosapi.video.infrastructure.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoDownloadUseCaseImpl implements VideoDownloadUseCase {

    private final VideoRepository videoRepository;
    private final AzureBlobStorageService azureBlobStorageService;

    @Override
    public VideoDownloadData prepareDownload(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found with id: " + videoId));

        String videoBlobName = video.getStoredFileName();
        if (!azureBlobStorageService.blobExists(videoBlobName)) {
            throw new IllegalStateException("Video blob not found in storage: " + videoBlobName);
        }

        String framePrefix = videoId + "/frames/";
        List<String> frameBlobs = azureBlobStorageService.listBlobsByPrefix(framePrefix);
        log.info("Found {} frame(s) for video {}", frameBlobs.size(), videoId);

        return VideoDownloadData.builder()
                .video(video)
                .videoBlobName(videoBlobName)
                .frameBlobNames(frameBlobs)
                .zipFileName("video-" + videoId + ".zip")
                .build();
    }
}

