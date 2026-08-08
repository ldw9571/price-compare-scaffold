package com.pricecompare.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 감시 키워드 등록/수정 요청 및 응답 DTO. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchKeywordDto {
    private Long id;
    private String keyword;
    private String category;
    private Integer priority;
    private Boolean active;
}
