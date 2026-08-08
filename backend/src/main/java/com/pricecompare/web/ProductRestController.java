package com.pricecompare.web;

import com.pricecompare.domain.price.PriceComparison;
import com.pricecompare.domain.price.PriceComparisonRepository;
import com.pricecompare.domain.product.WholesaleProduct;
import com.pricecompare.domain.product.WholesaleProductRepository;
import com.pricecompare.web.dto.ProductDetailDto;
import com.pricecompare.web.dto.ProductListItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 경쟁력 상품 리스트 / 상세 조회 API (React 대시보드용).
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final WholesaleProductRepository wholesaleProductRepository;
    private final PriceComparisonRepository priceComparisonRepository;

    /**
     * 경쟁력 상품 리스트 조회.
     *
     * TODO: 상품 수가 많아지면 findAll() + 인메모리 필터링은 비효율적입니다.
     *       그때는 PriceComparison에 "최신 여부" 플래그를 두거나 네이티브 쿼리(윈도우 함수)로
     *       DB 레벨에서 최신 1건 + 필터링 + 정렬 + 페이징을 처리하도록 개선이 필요합니다.
     *
     * @param category    카테고리 필터 (없으면 전체)
     * @param sort        정렬 기준: score(기본, 경쟁력 높은순) | price(도매가 낮은순)
     * @param minMargin   최소 마진율 필터 (예: 0.2 = 20% 이상만)
     * @param moqOneOnly  MOQ=1(위탁 가능)만 필터링
     */
    @GetMapping
    public List<ProductListItemDto> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "score") String sort,
            @RequestParam(required = false) BigDecimal minMargin,
            @RequestParam(defaultValue = "false") boolean moqOneOnly
    ) {
        List<WholesaleProduct> products = category != null && !category.isBlank()
                ? wholesaleProductRepository.findAll().stream()
                    .filter(p -> category.equals(p.getCategory())).toList()
                : wholesaleProductRepository.findAll();

        List<ProductListItemDto> result = products.stream()
                .map(product -> {
                    PriceComparison latest = priceComparisonRepository
                            .findTopByWholesaleProduct_IdOrderByScannedAtDesc(product.getId())
                            .orElse(null);
                    return ProductListItemDto.of(product, latest);
                })
                // 네이버 매칭이 안 된(가격비교 이력이 없는) 상품은 리스트에서 제외
                .filter(dto -> dto.getCompetitivenessScore() != null)
                .filter(dto -> !moqOneOnly || (dto.getMoq() != null && dto.getMoq() == 1))
                .filter(dto -> minMargin == null || (dto.getMarginRate() != null
                        && dto.getMarginRate().compareTo(minMargin) >= 0))
                .toList();

        Comparator<ProductListItemDto> comparator = "price".equals(sort)
                ? Comparator.comparing(ProductListItemDto::getWholesalePrice,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                : Comparator.comparing(ProductListItemDto::getCompetitivenessScore,
                        Comparator.nullsLast(Comparator.reverseOrder()));

        return result.stream().sorted(comparator).toList();
    }

    @GetMapping("/{id}")
    public ProductDetailDto getProductDetail(@PathVariable Long id) {
        WholesaleProduct product = wholesaleProductRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. id=" + id));

        List<PriceComparison> history =
                priceComparisonRepository.findByWholesaleProduct_IdOrderByScannedAtDesc(id);

        return ProductDetailDto.of(product, history);
    }

    /** 카테고리 목록 (필터 드롭다운용) */
    @GetMapping("/categories")
    public List<String> getCategories() {
        return wholesaleProductRepository.findAll().stream()
                .map(WholesaleProduct::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .toList();
    }
}
