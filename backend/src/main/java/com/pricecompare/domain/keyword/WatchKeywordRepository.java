package com.pricecompare.domain.keyword;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchKeywordRepository extends JpaRepository<WatchKeyword, Long> {

    List<WatchKeyword> findByActiveTrue();
}
