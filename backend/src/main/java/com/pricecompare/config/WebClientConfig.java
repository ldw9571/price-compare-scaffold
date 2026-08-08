package com.pricecompare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 도매꾹 / 네이버쇼핑 등 외부 API 호출에 사용할 WebClient 빈을 정의합니다.
 * 사이트별로 baseUrl이 다르므로, 클라이언트마다 별도 Bean으로 분리합니다.
 * 실제 baseUrl/타임아웃 값은 다음 단계(API 클라이언트 구현)에서 채웁니다.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient domeggookWebClient() {
        return WebClient.builder()
                .baseUrl("https://domeggook.com/ssl/api/")
                .build();
    }

    @Bean
    public WebClient naverWebClient() {
        return WebClient.builder()
                .baseUrl("https://openapi.naver.com")
                .build();
    }
}
