package com.pricecompare.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WholesaleProductRepository extends JpaRepository<WholesaleProduct, Long> {

    Optional<WholesaleProduct> findBySourceSiteAndSourceItemNo(String sourceSite, String sourceItemNo);
}
