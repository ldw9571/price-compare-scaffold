package com.pricecompare.external.naver;

import com.pricecompare.external.naver.dto.NaverShopItem;
import com.pricecompare.external.naver.dto.NaverShopSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 네이버쇼핑 검색 오픈API 연동 클라이언트.
 * 상품명으로 검색해 최저가 항목을 찾습니다.
 *
 * 참고: 네이버 오픈API는 검색어 매칭 기반이라, 도매꾹 상품명을 그대로 넣으면
 * 브랜드/모델명이 섞여 정확도가 떨어질 수 있습니다.
 * 필요시 다음 단계(시세비교 서비스)에서 상품명 전처리(불용어 제거 등)를 함께 다룹니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverShoppingClient {

    private final WebClient naverWebClient;

    @Value("${external-api.naver.client-id}")
    private String clientId;

    @Value("${external-api.naver.client-secret}")
    private String clientSecret;

    private static final String SEARCH_PATH = "/v1/search/shop.json";

    /**
     * 상품명으로 검색해서 결과 목록을 가져옵니다. (정렬: 가격 낮은 순)
     *
     * @param query   검색어 (도매 상품명)
     * @param display 가져올 건수 (최대 100)
     */
    public List<NaverShopItem> search(String query, int display) {
        try {
            NaverShopSearchResponse response = naverWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(SEARCH_PATH)
                            .queryParam("query", query)
                            .queryParam("display", display)
                            .queryParam("sort", "asc") // asc: 가격 낮은순, sim: 정확도순
                            .build())
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .retrieve()
                    .bodyToMono(NaverShopSearchResponse.class)
                    .block();

            if (response == null || response.getItems() == null) {
                log.warn("네이버쇼핑 검색 응답이 비어있습니다. query={}", query);
                return List.of();
            }
            return response.getItems();

        } catch (Exception e) {
            log.error("네이버쇼핑 검색 호출 실패. query={}, error={}", query, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 검색 결과 중 최저가(lprice) 1건을 찾습니다.
     * 이미 sort=asc로 요청하지만, 방어적으로 한 번 더 비교합니다.
     */
    public Optional<NaverShopItem> findLowestPrice(String query) {
        List<NaverShopItem> results = search(query, 20);
        return results.stream()
                .filter(item -> item.getLprice() != null && !item.getLprice().isBlank())
                .min(Comparator.comparingLong(item -> Long.parseLong(item.getLprice())));
    }
}
