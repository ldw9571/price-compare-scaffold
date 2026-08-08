package com.pricecompare.external.domeggook;

import com.pricecompare.external.domeggook.dto.DomeggookItem;
import com.pricecompare.external.domeggook.dto.DomeggookItemListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

/**
 * 도매꾹 Open API 연동 클라이언트.
 *
 * 호출 제한: 분당 180회 / 하루 15,000회. 배치에서 다건 호출 시 반드시 간격을 두어야 합니다
 * (예: 검색 1회당 최소 350ms 이상 sleep).
 *
 * API 키 미발급 상태에서는 실제 호출 시 도매꾹 서버에서 인증 오류가 반환됩니다.
 * 발급 후 최초 1회는 getItemList 결과를 로그로 찍어서(log.debug 주석 해제)
 * 실제 JSON 필드명이 DTO와 일치하는지 꼭 확인해 주세요.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomeggookClient {

    private final WebClient domeggookWebClient;

    @Value("${external-api.domeggook.api-key}")
    private String apiKey;

    private static final String VERSION = "4.1";
    private static final String MARKET = "dome"; // dome: 도매꾹, supply: 도매매

    /**
     * 키워드로 상품 목록을 조회합니다.
     *
     * @param keyword  검색어 (예: "음식물처리기")
     * @param page     페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 건수 (최대값은 API 정책 확인 필요, 우선 50 권장)
     */
    public List<DomeggookItem> getItemList(String keyword, int page, int pageSize) {
        try {
            DomeggookItemListResponse response = domeggookWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("")
                            .queryParam("ver", VERSION)
                            .queryParam("mode", "getItemList")
                            .queryParam("aid", apiKey)
                            .queryParam("market", MARKET)
                            .queryParam("om", "json")
                            .queryParam("kw", keyword)
                            .queryParam("sz", pageSize)
                            .queryParam("pg", page)
                            .queryParam("so", "rd") // rd: 등록일순. 정렬 기준은 필요시 조정
                            .build())
                    .retrieve()
                    .bodyToMono(DomeggookItemListResponse.class)
                    .block();

            // log.debug("도매꾹 getItemList raw response: {}", response);

            if (response == null || response.getDomeggook() == null
                    || response.getDomeggook().getList() == null
                    || response.getDomeggook().getList().getItem() == null) {
                log.warn("도매꾹 getItemList 응답이 비어있습니다. keyword={}", keyword);
                return Collections.emptyList();
            }
            return response.getDomeggook().getList().getItem();

        } catch (Exception e) {
            log.error("도매꾹 getItemList 호출 실패. keyword={}, error={}", keyword, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
