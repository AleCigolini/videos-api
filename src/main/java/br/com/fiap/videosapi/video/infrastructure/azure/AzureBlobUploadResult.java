package br.com.fiap.videosapi.video.infrastructure.azure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureBlobUploadResult {

    private String fileName;
    private String blobUrl;
    private String containerName;
    private Long fileSize;
    private String contentType;
    private boolean success;
    private String errorMessage;
}
