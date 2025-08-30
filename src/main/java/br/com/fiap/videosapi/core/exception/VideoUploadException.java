package br.com.fiap.videosapi.core.exception;

public class VideoUploadException extends VideoException {
    
    public VideoUploadException(String message) {
        super(message);
    }
    
    public VideoUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
