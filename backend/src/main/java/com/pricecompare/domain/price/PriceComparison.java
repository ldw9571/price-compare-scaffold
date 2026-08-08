package com.pricecompare.domain.price;

import com.pricecompare.domain.product.WholesaleProduct;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 도매 상품과 네이버쇼핑 최저가를 비교한 결과.
 * 스캔 시점마다 새로운 row로 쌓아서 가격 추이(히스토리)를 조회할 수 있게 합니다.
 */
@Entity
@Table(
        name = "price_comparison",
        indexes = {
                // 특정 상품의 가격 추이(히스토리) 조회용 복합 인덱스
                @Index(name = "idx_price_comparison_product_scanned", columnList = "wholesale_product_id, scanned_at"),
                // 대시보드 "경쟁력순 정렬"의 핵심 인덱스
                @Index(name = "idx_price_comparison_score", columnList = "competitiveness_score")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wholesale_product_id", nullable = false)
    private WholesaleProduct wholesaleProduct;

    @Column(name = "naver_lowest_price", precision = 12, scale = 2)
    private BigDecimal naverLowestPrice;

    @Column(name = "naver_product_url", length = 1000)
    private String naverProductUrl;

    @Column(name = "naver_mall_name", length = 200)
    private String naverMallName;

    /** (네이버 최저가 - (도매가+배송비+수수료+목표마진)) / 네이버 최저가 */
    @Column(name = "competitiveness_score", precision = 8, scale = 4)
    private BigDecimal competitivenessScore;

    @Column(name = "margin_rate", precision = 8, scale = 4)
    private BigDecimal marginRate;

    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;
}
