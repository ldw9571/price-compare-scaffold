package com.pricecompare.external.naver;

import java.nio.file.Files;
import java.nio.file.Path;

public class NaverCrawlerManualTest {

    public static void main(String[] args) throws Exception {
        NaverShoppingCrawler crawler = new NaverShoppingCrawler();

        String html = crawler.fetchRawHtml("마스크");

        System.out.println("HTML 총 길이: " + html.length());

        // __NEXT_DATA__ 유무와 상관없이 원본 HTML을 파일로 저장
        Path htmlPath = Path.of("naver_raw.html");
        Files.writeString(htmlPath, html);
        System.out.println("원본 HTML 저장 완료: " + htmlPath.toAbsolutePath());

        // 캡차/차단 페이지 여부를 빠르게 짐작할 수 있는 키워드 체크
        if (html.contains("captcha") || html.contains("보안")) {
            System.out.println("⚠️ 캡차/보안 관련 문구 감지됨");
        }
        if (html.contains("__NEXT_DATA__")) {
            System.out.println("✅ __NEXT_DATA__ 문자열은 존재함 (정규식 패턴만 문제일 수 있음)");
        } else {
            System.out.println("❌ __NEXT_DATA__ 문자열 자체가 없음 (다른 페이지 구조이거나 차단 페이지)");
        }
    }
}