package com.pricecompare.domain.product;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 도매사이트에서 수집한 원본 상품 정보.
 * sourceSite로 도매꾹/오너클랜 등 출처를 구분하여, 추후 사이트 확장 시에도
 * 하나의 테이블에서 통합 관리할 수 있도록 설계했습니다.
 */
@Entity
@Table(
        name = "wholesale_products",
        uniqueConstraints = {
                // 같은 출처 사이트의 같은 상품번호는 1건만 존재해야 함 (배치 재수집 시 upsert 기준)
                @UniqueConstraint(name = "uk_source_site_item_no", columnNames = {"source_site", "source_item_no"})
        },
        indexes = {
                @Index(name = "idx_wholesale_products_category", columnList = "category")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WholesaleProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 출처 사이트 구분: DOMEGGOOK, OWNERCLAN 등 (추후 확장) */
    @Column(name = "source_site", nullable = false, length = 30)
    private String sourceSite;

    /** 원본 사이트의 상품 번호 */
    @Column(name = "source_item_no", nullable = false, length = 50)
    private String sourceItemNo;

    @Column(name = "item_name", nullable = false, length = 500)
    private String itemName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "wholesale_price", precision = 12, scale = 2)
    private BigDecimal wholesalePrice;

    @Column(name = "shipping_fee", precision = 12, scale = 2)
    private BigDecimal shippingFee;

    /** 최소 주문 수량. 위탁판매 가능 여부 판단 기준(MOQ=1) */
    @Column(name = "moq")
    private Integer moq;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "item_url", length = 1000)
    private String itemUrl;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
