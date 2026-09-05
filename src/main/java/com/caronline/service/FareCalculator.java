package com.caronline.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 学习用计价：起步价 + 里程费。金额只用 BigDecimal。
 */
public final class FareCalculator {

    public static final BigDecimal START_PRICE = new BigDecimal("8.00");
    public static final BigDecimal PER_KM = new BigDecimal("2.40");

    private FareCalculator() {
    }

    public static BigDecimal calc(BigDecimal distanceKm) {
        return START_PRICE.add(PER_KM.multiply(distanceKm)).setScale(2, RoundingMode.HALF_UP);
    }

    public static String detailJson(BigDecimal distanceKm, BigDecimal fare) {
        return "{\"distanceKm\":" + distanceKm.toPlainString()
                + ",\"startPrice\":" + START_PRICE.toPlainString()
                + ",\"perKm\":" + PER_KM.toPlainString()
                + ",\"fare\":" + fare.toPlainString()
                + "}";
    }
}
