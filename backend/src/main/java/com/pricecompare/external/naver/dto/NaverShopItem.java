package com.pricecompare.external.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 네이버쇼핑 검색 API 응답의 상품 1건.
 * 공식 문서 기준 필드명: title, link, image, lprice, hprice, mallName,
 * productId, productType, brand, maker, category1~4
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverShopItem {

    /** HTML 태그(<b> 등)가 포함될 수 있어 저장 전 strip 필요 */
    @JsonProperty("title")
    private String title;

    @JsonProperty("link")
    private String link;

    @JsonProperty("image")
    private String image;

    /** 최저가 */
    @JsonProperty("lprice")
    private String lprice;

    @JsonProperty("hprice")
    private String hprice;

    @JsonProperty("mallName")
    private String mallName;

    @JsonProperty("productId")
    private String productId;

    /** 1: 일반상품, 2: 중고, 3: 단종, 4: 판매예정 */
    @JsonProperty("productType")
    private String productType;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("maker")
    private String maker;

    @JsonProperty("category1")
    private String category1;

    @JsonProperty("category2")
    private String category2;
}
