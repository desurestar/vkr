package ru.zagrebin.server.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zagrebin.server.data.entity.StatisticsSettingsEntity;
import java.util.Optional;

public interface StatisticsSettingsRepository extends JpaRepository<StatisticsSettingsEntity, Long> {
    Optional<StatisticsSettingsEntity> findByUserId(Long userId);
}
