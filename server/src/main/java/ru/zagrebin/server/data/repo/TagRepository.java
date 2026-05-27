package ru.zagrebin.server.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zagrebin.server.data.entity.TagEntity;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Long> {
    Optional<TagEntity> findByNameIgnoreCase(String name);
    List<TagEntity> findByNameContainingIgnoreCase(String q);
}
