package com.pricecompare.service;

import com.pricecompare.domain.product.WholesaleProduct;
import com.pricecompare.domain.product.WholesaleProductRepository;
import com.pricecompare.external.domeggook.DomeggookClient;
import com.pricecompare.external.domeggook.dto.DomeggookItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 도매꾹에서 키워드 기준으로 상품을 수집하여 저장(upsert)하는 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCollectService {

    private static final String SOURCE_SITE = "DOMEGGOOK";

    private final DomeggookClient domeggookClient;
    private final WholesaleProductRepository wholesaleProductRepository;

    /**
     * 키워드로 상품을 수집해서 저장하고, 저장/갱신된 상품 목록을 반환합니다.
     *
     * @param keyword  검색어
     * @param category 감시 키워드에 매핑된 카테고리(저장용 태그, 없으면 null)
     */
    public List<WholesaleProduct> collectByKeyword(String keyword, String category) {
        // 우선 1페이지(최대 50건)만 수집합니다. 페이지네이션 전체 순회는
        // 호출량이 많아지므로 필요성이 확인되면 이후 확장합니다.
        List<DomeggookItem> items = domeggookClient.getItemList(keyword, 1, 50);

        List<WholesaleProduct> saved = new ArrayList<>();
        for (DomeggookItem item : items) {
            try {
                saved.add(upsert(item, category));
            } catch (Exception e) {
                log.warn("상품 저장 실패. sourceItemNo={}, error={}", item.getNo(), e.getMessage());
            }
        }
        log.info("도매꾹 수집 완료. keyword={}, 조회건수={}, 저장건수={}", keyword, items.size(), saved.size());
        return saved;
    }

    private WholesaleProduct upsert(DomeggookItem item, String category) {
        Optional<WholesaleProduct> existing =
                wholesaleProductRepository.findBySourceSiteAndSourceItemNo(SOURCE_SITE, item.getNo());

        LocalDateTime now = LocalDateTime.now();

        WholesaleProduct product = existing.orElseGet(() -> WholesaleProduct.builder()
                .sourceSite(SOURCE_SITE)
                .sourceItemNo(item.getNo())
                .collectedAt(now)
                .build());

        product.setItemName(item.getTitle());
        product.setCategory(category);
        product.setWholesalePrice(parseDecimal(item.getPrice()));
        product.setShippingFee(item.getDeli() != null ? parseDecimal(item.getDeli().getFee()) : BigDecimal.ZERO);
        product.setMoq(parseMoq(item.getUnitQty()));
        // 도매꾹 목록 API 응답에 재고 수량 필드가 없어 우선 null로 둡니다.
        // 필요시 상품상세 API(getItemDetail)를 추가 연동해서 채우는 것을 다음 확장으로 고려합니다.
        product.setStockQuantity(null);
        product.setItemUrl(item.getUrl());
        product.setUpdatedAt(now);

        return wholesaleProductRepository.save(product);
    }

    private BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Integer parseMoq(String raw) {
        if (raw == null || raw.isBlank()) return 1;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
