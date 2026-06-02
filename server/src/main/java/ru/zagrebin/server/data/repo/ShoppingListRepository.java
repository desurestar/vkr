package ru.zagrebin.server.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zagrebin.server.data.entity.ShoppingListEntity;

import java.util.List;

public interface ShoppingListRepository extends JpaRepository<ShoppingListEntity, Long> {
    List<ShoppingListEntity> findByUserIdOrderByIdAsc(Long userId);
}
