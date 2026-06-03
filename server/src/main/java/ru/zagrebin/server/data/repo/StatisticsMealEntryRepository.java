package ru.zagrebin.server.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zagrebin.server.data.entity.StatisticsMealEntryEntity;
import java.time.LocalDate;
import java.util.List;

public interface StatisticsMealEntryRepository extends JpaRepository<StatisticsMealEntryEntity, Long> {
    List<StatisticsMealEntryEntity> findByUserIdAndDateBetweenOrderByCreatedAtAsc(Long userId, LocalDate start, LocalDate end);
    void deleteByUserIdAndDateBefore(Long userId, LocalDate cutoff);
}
