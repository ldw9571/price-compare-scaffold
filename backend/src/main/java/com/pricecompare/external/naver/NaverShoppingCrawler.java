package com.pricecompare.external.naver;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * [BLOCKED - 2026-08-19] 네이버쇼핑 Jsoup 크롤링 중단.
 *
 * 증상: fetchRawHtml() 정상 응답(status=200)이지만, 실제 검색결과 HTML이 아니라
 *       nid.naver.com 로그인 페이지(action="https://nid.naver.com/nidlogin.login")로
 *       리다이렉트됨. HTML 길이 약 19KB (정상 검색결과는 수십만 자 이상이어야 함).
 *
 * 원인 추정: 네이버가 User-Agent/헤더 기반 요청을 봇으로 판단, 로그인 월(wall) 적용.
 *           단순 헤더 위장(Jsoup)으로는 우회 불가한 수준.
 *
 * 시도했던 것:
 *   - Accept-Encoding에서 br(Brotli) 제거 (Jsoup 미지원) → 효과 없음, 근본 원인 아니었음
 *   - 정규식 __NEXT_DATA__ 패턴 완화 → 무의미 (애초에 검색결과 페이지 자체가 안 옴)
 *
 * 재개 시 고려할 방법:
 *   1. Selenium/Playwright 등 headless 브라우저 자동화 (JS 실행 + 세션/쿠키 유지)
 *      - 단, 동일한 차단 로직에 결국 걸릴 가능성 있음
 *   2. 실 브라우저 세션 쿠키를 수동 추출해 Jsoup 요청에 주입 (임시방편, 쿠키 만료시 재작업 필요)
 *   3. 네이버 커머스API센터 등 공식 API 대체 경로 재조사
 *
 * 현재 우선순위 낮춤 - 쿠팡 파트너스 연동으로 리테일 가격비교 소스 전환 (2026-08-19 결정).
 */

@Slf4j
@Component
public class NaverShoppingCrawler {

    private static final String SEARCH_URL = "https://search.shopping.naver.com/search/all";

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    // 속성 순서(id/type)가 바뀌어도 매칭되도록 완화
    private static final Pattern NEXT_DATA_PATTERN = Pattern.compile(
            "<script[^>]*id=\"__NEXT_DATA__\"[^>]*>(.*?)</script>",
            Pattern.DOTALL
    );

    public String fetchRawHtml(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            Connection.Response response = Jsoup.connect(SEARCH_URL + "?query=" + encodedQuery)
                    .userAgent(USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    // br(Brotli) 제거 - Jsoup이 Brotli 디코딩을 지원하지 않아 응답이 깨질 수 있음
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("sec-ch-ua", "\"Chromium\";v=\"126\", \"Not.A/Brand\";v=\"24\", \"Google Chrome\";v=\"126\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"Windows\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .maxBodySize(0)
                    .timeout(15_000)
                    .execute();

            log.info("네이버쇼핑 검색 응답 status={}, query={}", response.statusCode(), query);
            return response.body();

        } catch (IOException e) {
            log.error("네이버쇼핑 HTML 요청 실패. query={}, error={}", query, e.getMessage(), e);
            throw new RuntimeException("네이버쇼핑 크롤링 실패: " + query, e);
        }
    }

    public String extractNextDataJson(String html) {
        Matcher matcher = NEXT_DATA_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        log.warn("__NEXT_DATA__ 스크립트 태그를 찾지 못했습니다. (차단/캡차 페이지일 가능성)");
        throw new IllegalStateException("__NEXT_DATA__ 추출 실패 - HTML 구조 확인 필요");
    }
}