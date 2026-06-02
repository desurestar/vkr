package ru.zagrebin.server.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zagrebin.server.data.entity.PostEntity;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByTypeIgnoreCaseAndTitleContainingIgnoreCase(String type, String q);
    List<PostEntity> findByTypeIgnoreCase(String type);
    List<PostEntity> findByTitleContainingIgnoreCase(String q);
    List<PostEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
