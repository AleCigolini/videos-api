package br.com.fiap.videosapi.video.infrastructure.repository;

import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByStatus(VideoStatus status);
    List<Video> findAllByUserId(String userId);
    List<Video> findByStatusAndUserId(VideoStatus status, String userId);
    Optional<Video> findByIdAndUserId(Long id, String userId);
}
