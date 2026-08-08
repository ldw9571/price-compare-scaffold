package com.pricecompare.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 경쟁력 스코어 계산 로직.
 * score = (네이버 최저가 - (도매가 + 배송비 + 수수료 + 목표마진)) / 네이버 최저가
 * marginRate = (네이버 최저가 - (도매가 + 배송비 + 수수료)) / 네이버 최저가  (목표마진 반영 전 실질 마진율)
 */
@Component
public class ScoreCalculator {

    @Value("${scoring.commission-rate}")
    private BigDecimal commissionRate;

    @Value("${scoring.target-margin}")
    private BigDecimal targetMargin;

    private static final MathContext MC = new MathContext(8, RoundingMode.HALF_UP);

    public Result calculate(BigDecimal naverLowestPrice, BigDecimal wholesalePrice, BigDecimal shippingFee) {
        if (naverLowestPrice == null || naverLowestPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return new Result(BigDecimal.ZERO, BigDecimal.ZERO);
        }
        BigDecimal safeWholesale = nvl(wholesalePrice);
        BigDecimal safeShipping = nvl(shippingFee);
        BigDecimal commission = naverLowestPrice.multiply(commissionRate, MC);

        BigDecimal totalCostWithoutMargin = safeWholesale.add(safeShipping).add(commission);
        BigDecimal totalCostWithMargin = totalCostWithoutMargin.add(targetMargin);

        BigDecimal score = naverLowestPrice.subtract(totalCostWithMargin)
                .divide(naverLowestPrice, MC);
        BigDecimal marginRate = naverLowestPrice.subtract(totalCostWithoutMargin)
                .divide(naverLowestPrice, MC);

        return new Result(score, marginRate);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /** score: 경쟁력 스코어, marginRate: 목표마진 반영 전 실질 마진율 */
    public record Result(BigDecimal score, BigDecimal marginRate) {
    }
}
