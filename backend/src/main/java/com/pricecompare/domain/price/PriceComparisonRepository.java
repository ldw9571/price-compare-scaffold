package com.pricecompare.domain.price;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceComparisonRepository extends JpaRepository<PriceComparison, Long> {

    List<PriceComparison> findByWholesaleProduct_IdOrderByScannedAtDesc(Long wholesaleProductId);

    Optional<PriceComparison> findTopByWholesaleProduct_IdOrderByScannedAtDesc(Long wholesaleProductId);
}
