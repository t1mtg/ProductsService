package com.timotege.dao.repository;

import com.timotege.dao.model.ShopUnitStatisticUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryRepository extends JpaRepository<ShopUnitStatisticUnit, Long> {
    List<ShopUnitStatisticUnit> findByDateTimeGreaterThanEqualAndDateTimeLessThanAndUnitId(LocalDateTime dateTime1, LocalDateTime dateTime2, UUID id);
    void deleteAllByUnitId(UUID id);
}
