package ru.zagrebin.server.data.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.zagrebin.server.data.entity.PostEntity;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByTypeIgnoreCaseAndStatusIgnoreCaseAndTitleContainingIgnoreCase(String type, String status, String q);
    List<PostEntity> findByTypeIgnoreCaseAndStatusIgnoreCaseAndTitleContainingIgnoreCase(String type, String status, String q, Pageable pageable);
    List<PostEntity> findByTypeIgnoreCaseAndStatusIgnoreCase(String type, String status);
    List<PostEntity> findByTypeIgnoreCaseAndStatusIgnoreCase(String type, String status, Pageable pageable);
    List<PostEntity> findByStatusIgnoreCaseAndTitleContainingIgnoreCase(String status, String q);
    List<PostEntity> findByAuthorIdAndStatusIgnoreCaseOrderByCreatedAtDesc(Long authorId, String status);
}
