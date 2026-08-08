package com.pricecompare.web;

import com.pricecompare.domain.keyword.WatchKeyword;
import com.pricecompare.domain.keyword.WatchKeywordRepository;
import com.pricecompare.web.dto.WatchKeywordDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감시 키워드/카테고리 관리 API.
 */
@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class KeywordRestController {

    private final WatchKeywordRepository watchKeywordRepository;

    @GetMapping
    public List<WatchKeywordDto> getKeywords() {
        return watchKeywordRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping
    public WatchKeywordDto createKeyword(@RequestBody @Valid CreateKeywordRequest request) {
        WatchKeyword keyword = WatchKeyword.builder()
                .keyword(request.keyword())
                .category(request.category())
                .priority(request.priority() != null ? request.priority() : 99)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        return toDto(watchKeywordRepository.save(keyword));
    }

    @PutMapping("/{id}")
    public WatchKeywordDto updateKeyword(@PathVariable Long id, @RequestBody UpdateKeywordRequest request) {
        WatchKeyword keyword = watchKeywordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("키워드를 찾을 수 없습니다. id=" + id));

        if (request.category() != null) keyword.setCategory(request.category());
        if (request.priority() != null) keyword.setPriority(request.priority());
        if (request.active() != null) keyword.setActive(request.active());

        return toDto(watchKeywordRepository.save(keyword));
    }

    @DeleteMapping("/{id}")
    public void deleteKeyword(@PathVariable Long id) {
        watchKeywordRepository.deleteById(id);
    }

    private WatchKeywordDto toDto(WatchKeyword entity) {
        return WatchKeywordDto.builder()
                .id(entity.getId())
                .keyword(entity.getKeyword())
                .category(entity.getCategory())
                .priority(entity.getPriority())
                .active(entity.getActive())
                .build();
    }

    public record CreateKeywordRequest(
            @NotBlank(message = "키워드는 필수입니다") String keyword,
            String category,
            Integer priority
    ) {}

    public record UpdateKeywordRequest(String category, Integer priority, Boolean active) {}
}
