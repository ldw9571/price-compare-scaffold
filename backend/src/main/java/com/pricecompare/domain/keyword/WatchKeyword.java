package com.pricecompare.domain.keyword;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 모니터링 대상 키워드/카테고리.
 * 배치 스캔 시 이 테이블을 기준으로 도매꾹 검색을 반복 실행합니다.
 */
@Entity
@Table(
        name = "watch_keywords",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_keyword", columnNames = {"keyword"})
        },
        indexes = {
                @Index(name = "idx_watch_keywords_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false, length = 200)
    private String keyword;

    @Column(name = "category", length = 100)
    private String category;

    /** 1순위 관심 카테고리(음식물처리기) 등 우선순위 표시용 */
    @Column(name = "priority")
    private Integer priority;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
