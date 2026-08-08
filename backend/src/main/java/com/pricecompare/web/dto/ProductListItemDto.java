package com.pricecompare.web.dto;

import com.pricecompare.domain.price.PriceComparison;
import com.pricecompare.domain.product.WholesaleProduct;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경쟁력 상품 리스트(대시보드) 조회용 응답 DTO.
 * WholesaleProduct + 최신 PriceComparison을 합쳐서 내려줍니다.
 */
@Getter
@Builder
public class ProductListItemDto {
    private Long id;
    private String itemName;
    private String category;
    private BigDecimal wholesalePrice;
    private BigDecimal shippingFee;
    private Integer moq;
    private Integer stockQuantity;
    private String itemUrl;

    private BigDecimal naverLowestPrice;
    private String naverProductUrl;
    private String naverMallName;
    private BigDecimal competitivenessScore;
    private BigDecimal marginRate;
    private LocalDateTime scannedAt;

    public static ProductListItemDto of(WholesaleProduct product, PriceComparison latest) {
        ProductListItemDtoBuilder builder = ProductListItemDto.builder()
                .id(product.getId())
                .itemName(product.getItemName())
                .category(product.getCategory())
                .wholesalePrice(product.getWholesalePrice())
                .shippingFee(product.getShippingFee())
                .moq(product.getMoq())
                .stockQuantity(product.getStockQuantity())
                .itemUrl(product.getItemUrl());

        if (latest != null) {
            builder.naverLowestPrice(latest.getNaverLowestPrice())
                    .naverProductUrl(latest.getNaverProductUrl())
                    .naverMallName(latest.getNaverMallName())
                    .competitivenessScore(latest.getCompetitivenessScore())
                    .marginRate(latest.getMarginRate())
                    .scannedAt(latest.getScannedAt());
        }
        return builder.build();
    }
}
