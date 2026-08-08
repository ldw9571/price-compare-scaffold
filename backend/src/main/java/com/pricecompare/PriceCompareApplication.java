package com.pricecompare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 매일 새벽 배치(스캔) 스케줄러 사용을 위해 활성화
public class PriceCompareApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriceCompareApplication.class, args);
    }
}
