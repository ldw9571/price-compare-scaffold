package com.pricecompare.domain.log;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 배치 스캔 실행 이력. 성공/실패 여부와 수집 건수를 기록하여
 * 대시보드의 "스캔 이력/로그" 화면에서 조회합니다.
 */
@Entity
@Table(
        name = "scan_logs",
        indexes = {
                @Index(name = "idx_scan_logs_started_at", columnList = "started_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FULL(전체 재스캔), KEYWORD(단일 키워드) 등 */
    @Column(name = "scan_type", nullable = false, length = 30)
    private String scanType;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    /** SUCCESS, FAILED, RUNNING */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "items_collected")
    private Integer itemsCollected;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}
