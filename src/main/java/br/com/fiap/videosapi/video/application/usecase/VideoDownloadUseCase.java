package br.com.fiap.videosapi.video.application.usecase;

import br.com.fiap.videosapi.video.application.usecase.dto.VideoDownloadData;

public interface VideoDownloadUseCase {
    VideoDownloadData prepareDownload(Long videoId, String idCliente);
}

