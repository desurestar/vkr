package ru.zagrebin.server.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zagrebin.server.data.entity.ShoppingItemEntity;

import java.util.List;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItemEntity, Long> {
    List<ShoppingItemEntity> findByUserId(Long userId);
}
