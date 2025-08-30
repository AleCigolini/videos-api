package br.com.fiap.videosapi.video.infrastructure.repository;

import br.com.fiap.videosapi.video.domain.entity.Video;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByStatus(VideoStatus status);

    List<Video> findByUploadedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Optional<Video> findByStoredFileName(String storedFileName);

    @Query("SELECT v FROM Video v WHERE v.originalFileName LIKE %:fileName%")
    List<Video> findByOriginalFileNameContaining(@Param("fileName") String fileName);

    long countByStatus(VideoStatus status);
}
