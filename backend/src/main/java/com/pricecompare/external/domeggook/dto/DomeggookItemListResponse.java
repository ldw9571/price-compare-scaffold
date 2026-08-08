package com.pricecompare.external.domeggook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 도매꾹 getItemList 응답 전체 구조.
 *
 * 실제 응답은 최상위가 "domeggook" 키로 한 번 더 감싸져 있습니다.
 * {
 *   "domeggook": {
 *     "header": { ... },
 *     "list": { "item": [ ... ] }
 *   }
 * }
 * 그래서 이 클래스가 최상위 응답이고, 실제 내용은 getDomeggook()을 한 번 더 거쳐야 합니다.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DomeggookItemListResponse {

    @JsonProperty("domeggook")
    private Body domeggook;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {

        @JsonProperty("header")
        private Header header;

        @JsonProperty("list")
        private ItemListWrapper list;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JsonProperty("numberOfItems")
        private Integer numberOfItems;

        @JsonProperty("currentPage")
        private Integer currentPage;

        @JsonProperty("numberOfPages")
        private Integer numberOfPages;

        @JsonProperty("itemsPerPage")
        private Integer itemsPerPage;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemListWrapper {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        @JsonProperty("item")
        private List<DomeggookItem> item;
    }
}