package edu.itc.se.shop.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OrderPricingCalculator {

    public record LinePrice(int qty, BigDecimal unitPrice) {}

    public static BigDecimal computeTotal(List<LinePrice> lines, BigDecimal taxRate) {
        if (lines == null || lines.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal effectiveTaxRate = (taxRate != null) ? taxRate : BigDecimal.ZERO;

        BigDecimal netTotal = BigDecimal.ZERO;
        for (LinePrice line : lines) {
            BigDecimal lineNet = line.unitPrice().multiply(BigDecimal.valueOf(line.qty()));
            netTotal = netTotal.add(lineNet);
        }

        BigDecimal grossTotal = netTotal.multiply(BigDecimal.ONE.add(effectiveTaxRate));
        return grossTotal.setScale(2, RoundingMode.HALF_UP);
    }
}
