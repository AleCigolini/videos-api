package br.com.fiap.videosapi.video.application.usecase.dto;

import br.com.fiap.videosapi.video.domain.entity.Video;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VideoDownloadData {
    private Video video;
    private String videoBlobName;
    private List<String> frameBlobNames;
    private String zipFileName;
}

