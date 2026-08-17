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

@Slf4j
@Component
public class NaverShoppingCrawler {

    private static final String SEARCH_URL = "https://search.shopping.naver.com/search/all";

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    private static final Pattern NEXT_DATA_PATTERN = Pattern.compile(
            "<script id=\"__NEXT_DATA__\" type=\"application/json\">(.*?)</script>",
            Pattern.DOTALL
    );

    public String fetchRawHtml(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            Connection.Response response = Jsoup.connect(SEARCH_URL + "?query=" + encodedQuery)
                    .userAgent(USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Accept-Encoding", "gzip, deflate, br")
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