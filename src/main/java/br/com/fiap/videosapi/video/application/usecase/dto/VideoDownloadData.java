package br.com.fiap.videosapi.video.application.usecase.dto;

import br.com.fiap.videosapi.video.domain.entity.Video;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDownloadData {
    private Video video;
    private String videoBlobName;
    private String zipFileName;
}
