package com.pricecompare.web;

import com.pricecompare.domain.log.ScanLog;
import com.pricecompare.domain.log.ScanLogRepository;
import com.pricecompare.service.ScanBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 스캔 이력/로그 조회 + 수동 실행 API.
 */
@RestController
@RequestMapping("/api/scan-logs")
@RequiredArgsConstructor
public class ScanLogRestController {

    private final ScanLogRepository scanLogRepository;
    private final ScanBatchService scanBatchService;

    @GetMapping
    public List<ScanLog> getScanLogs() {
        return scanLogRepository.findAllByOrderByStartedAtDesc();
    }

    /**
     * 수동으로 스캔을 즉시 실행합니다. (테스트/확인용)
     * 활성 키워드 수와 상품 수에 따라 시간이 오래 걸릴 수 있어 동기 방식으로 두었습니다.
     * 상품 수가 많아지면 비동기(@Async) 처리로 전환을 고려해야 합니다.
     */
    @PostMapping("/run")
    public ScanLog runScanManually() {
        return scanBatchService.runScan("MANUAL");
    }
}
