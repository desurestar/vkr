package ru.zagrebin.server.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zagrebin.server.data.entity.StatisticsDayEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StatisticsDayRepository extends JpaRepository<StatisticsDayEntity, Long> {
    Optional<StatisticsDayEntity> findByUserIdAndDate(Long userId, LocalDate date);
    List<StatisticsDayEntity> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    void deleteByUserIdAndDateBefore(Long userId, LocalDate cutoff);
}
