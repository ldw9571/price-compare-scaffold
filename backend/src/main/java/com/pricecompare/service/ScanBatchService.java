package com.pricecompare.service;

import com.pricecompare.domain.keyword.WatchKeyword;
import com.pricecompare.domain.keyword.WatchKeywordRepository;
import com.pricecompare.domain.log.ScanLog;
import com.pricecompare.domain.log.ScanLogRepository;
import com.pricecompare.domain.product.WholesaleProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 매일 새벽 전체 재스캔을 실행하는 배치 오케스트레이션 서비스.
 * 활성화된 감시 키워드를 순회하며 [도매꾹 수집 → 네이버 비교/스코어링]을 반복합니다.
 *
 * 호출량 관리: 도매꾹은 분당 180회 제한이 있어 키워드마다 호출 간격을 둡니다.
 * 네이버는 상품 1건마다 호출되므로 별도로 더 짧은 간격을 둡니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScanBatchService {

    private final ProductCollectService productCollectService;
    private final PriceCompareService priceCompareService;
    private final WatchKeywordRepository watchKeywordRepository;
    private final ScanLogRepository scanLogRepository;

    @Value("${batch.domeggook-call-interval-ms}")
    private long domeggookCallIntervalMs;

    @Value("${batch.naver-call-interval-ms}")
    private long naverCallIntervalMs;

    /** 매일 새벽 3시 실행 */
    @Scheduled(cron = "0 0 3 * * *")
    public void runDailyScan() {
        runScan("FULL");
    }

    /**
     * 실제 스캔 로직. 스케줄러뿐 아니라 수동 트리거(REST API)에서도 재사용합니다.
     */
    public ScanLog runScan(String scanType) {
        ScanLog scanLog = ScanLog.builder()
                .scanType(scanType)
                .startedAt(LocalDateTime.now())
                .status("RUNNING")
                .itemsCollected(0)
                .build();
        scanLog = scanLogRepository.save(scanLog);

        int totalCollected = 0;
        try {
            List<WatchKeyword> activeKeywords = watchKeywordRepository.findByActiveTrue();
            log.info("스캔 시작. 활성 키워드 {}건", activeKeywords.size());

            for (WatchKeyword watchKeyword : activeKeywords) {
                List<WholesaleProduct> products = productCollectService.collectByKeyword(
                        watchKeyword.getKeyword(), watchKeyword.getCategory());
                totalCollected += products.size();

                for (WholesaleProduct product : products) {
                    priceCompareService.compareAndScore(product);
                    sleep(naverCallIntervalMs);
                }

                sleep(domeggookCallIntervalMs);
            }

            scanLog.setStatus("SUCCESS");
        } catch (Exception e) {
            log.error("스캔 배치 실패: {}", e.getMessage(), e);
            scanLog.setStatus("FAILED");
            scanLog.setErrorMessage(truncate(e.getMessage(), 2000));
        } finally {
            scanLog.setFinishedAt(LocalDateTime.now());
            scanLog.setItemsCollected(totalCollected);
            scanLogRepository.save(scanLog);
        }

        return scanLog;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String truncate(String message, int maxLength) {
        if (message == null) return null;
        return message.length() > maxLength ? message.substring(0, maxLength) : message;
    }
}
