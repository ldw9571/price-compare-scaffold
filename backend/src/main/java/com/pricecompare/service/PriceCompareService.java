package com.pricecompare.service;

import com.pricecompare.common.ScoreCalculator;
import com.pricecompare.domain.price.PriceComparison;
import com.pricecompare.domain.price.PriceComparisonRepository;
import com.pricecompare.domain.product.WholesaleProduct;
import com.pricecompare.external.naver.NaverShoppingClient;
import com.pricecompare.external.naver.dto.NaverShopItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 도매 상품과 네이버쇼핑 최저가를 비교하고 경쟁력 점수를 계산해 저장하는 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCompareService {

    private final NaverShoppingClient naverShoppingClient;
    private final PriceComparisonRepository priceComparisonRepository;
    private final ScoreCalculator scoreCalculator;

    /**
     * 상품명으로 네이버 최저가를 조회하고, 경쟁력 점수를 계산해 PriceComparison으로 저장합니다.
     * 네이버 검색 결과가 없으면 저장하지 않고 빈 Optional을 반환합니다.
     */
    public Optional<PriceComparison> compareAndScore(WholesaleProduct product) {
        Optional<NaverShopItem> lowest = naverShoppingClient.findLowestPrice(product.getItemName());

        if (lowest.isEmpty()) {
            log.debug("네이버 최저가 매칭 실패. itemName={}", product.getItemName());
            return Optional.empty();
        }

        NaverShopItem naverItem = lowest.get();
        BigDecimal naverPrice = parseDecimal(naverItem.getLprice());

        ScoreCalculator.Result result = scoreCalculator.calculate(
                naverPrice, product.getWholesalePrice(), product.getShippingFee());

        PriceComparison comparison = PriceComparison.builder()
                .wholesaleProduct(product)
                .naverLowestPrice(naverPrice)
                .naverProductUrl(naverItem.getLink())
                .naverMallName(naverItem.getMallName())
                .competitivenessScore(result.score())
                .marginRate(result.marginRate())
                .scannedAt(LocalDateTime.now())
                .build();

        return Optional.of(priceComparisonRepository.save(comparison));
    }

    private BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
