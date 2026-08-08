package com.pricecompare.web.dto;

import com.pricecompare.domain.price.PriceComparison;
import com.pricecompare.domain.product.WholesaleProduct;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 상품 상세 + 가격 추이(히스토리) 조회용 응답 DTO. */
@Getter
@Builder
public class ProductDetailDto {
    private Long id;
    private String itemName;
    private String category;
    private BigDecimal wholesalePrice;
    private BigDecimal shippingFee;
    private Integer moq;
    private Integer stockQuantity;
    private String itemUrl;
    private LocalDateTime collectedAt;

    private List<PriceHistoryPoint> priceHistory;

    public static ProductDetailDto of(WholesaleProduct product, List<PriceComparison> history) {
        return ProductDetailDto.builder()
                .id(product.getId())
                .itemName(product.getItemName())
                .category(product.getCategory())
                .wholesalePrice(product.getWholesalePrice())
                .shippingFee(product.getShippingFee())
                .moq(product.getMoq())
                .stockQuantity(product.getStockQuantity())
                .itemUrl(product.getItemUrl())
                .collectedAt(product.getCollectedAt())
                .priceHistory(history.stream().map(PriceHistoryPoint::of).toList())
                .build();
    }

    @Getter
    @Builder
    public static class PriceHistoryPoint {
        private LocalDateTime scannedAt;
        private BigDecimal naverLowestPrice;
        private BigDecimal competitivenessScore;
        private BigDecimal marginRate;
        private String naverProductUrl;

        public static PriceHistoryPoint of(PriceComparison pc) {
            return PriceHistoryPoint.builder()
                    .scannedAt(pc.getScannedAt())
                    .naverLowestPrice(pc.getNaverLowestPrice())
                    .competitivenessScore(pc.getCompetitivenessScore())
                    .marginRate(pc.getMarginRate())
                    .naverProductUrl(pc.getNaverProductUrl())
                    .build();
        }
    }
}
