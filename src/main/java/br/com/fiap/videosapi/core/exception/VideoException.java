package br.com.fiap.videosapi.core.exception;

public class VideoException extends RuntimeException {
    
    public VideoException(String message) {
        super(message);
    }
    
    public VideoException(String message, Throwable cause) {
        super(message, cause);
    }
}
