package com.pricecompare.external.domeggook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 도매꾹 getItemList 응답의 상품 1건.
 * XML 응답 기준 필드명을 그대로 매핑했습니다. (no, title, price, unitQty, deli.fee 등)
 *
 * 검증 필요: 실제 API 키 발급 후 아래처럼 raw 응답을 한 번 찍어서
 * 필드명이 정확히 일치하는지 확인해 주세요.
 *   System.out.println(response); // DomeggookClient에서 임시로 로그 추가
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DomeggookItem {

    /** 상품번호 (source_item_no로 저장) */
    @JsonProperty("no")
    private String no;

    @JsonProperty("title")
    private String title;

    @JsonProperty("thumb")
    private String thumb;

    /** 판매회원 아이디 */
    @JsonProperty("id")
    private String sellerId;

    /** 도매가 */
    @JsonProperty("price")
    private String price;

    /** 단위 수량 - MOQ(최소구매수량) 판단에 활용 */
    @JsonProperty("unitQty")
    private String unitQty;

    /** 사업자(도매)회원 전용 상품 여부 */
    @JsonProperty("comOnly")
    private Boolean comOnly;

    @JsonProperty("adultOnly")
    private Boolean adultOnly;

    @JsonProperty("deli")
    private Delivery deli;

    @JsonProperty("url")
    private String url;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delivery {
        /** 배송비 부담 주체 (P: 판매자, B: 구매자 등) */
        @JsonProperty("who")
        private String who;

        /** 배송비 */
        @JsonProperty("fee")
        private String fee;

        /** 도서산간 추가배송비 여부 */
        @JsonProperty("add")
        private Boolean add;

        @JsonProperty("fromOversea")
        private Boolean fromOversea;
    }
}
