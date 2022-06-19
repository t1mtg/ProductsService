package com.timotege.dao.repository;

import com.timotege.dao.model.ShopUnit;
import com.timotege.dao.model.ShopUnitImport;
import com.timotege.dao.model.ShopUnitStatisticUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ShopUnit, UUID> {
    List<ShopUnit> findByDateGreaterThanEqualAndDateLessThanEqual(LocalDateTime dateTime1, LocalDateTime dateTime2);
}
