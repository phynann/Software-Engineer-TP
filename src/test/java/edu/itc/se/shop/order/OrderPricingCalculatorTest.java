package edu.itc.se.shop.order;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderPricingCalculatorTest {

    @Test
    void computeTotalWithZeroTax() {
        var lines = List.of(
                new OrderPricingCalculator.LinePrice(2, new BigDecimal("10.00")),
                new OrderPricingCalculator.LinePrice(1, new BigDecimal("25.50"))
        );
        BigDecimal total = OrderPricingCalculator.computeTotal(lines, BigDecimal.ZERO);
        assertEquals(new BigDecimal("45.50"), total);
    }

    @Test
    void computeTotalWithTaxAndRounding() {
        // Line 1: 2 * 49.90 = 99.80
        // Line 2: 1 * 10.00 = 10.00
        // Net: 109.80
        // Tax (20%): 109.80 * 1.20 = 131.76
        var lines = List.of(
                new OrderPricingCalculator.LinePrice(2, new BigDecimal("49.90")),
                new OrderPricingCalculator.LinePrice(1, new BigDecimal("10.00"))
        );
        BigDecimal total = OrderPricingCalculator.computeTotal(lines, new BigDecimal("0.20"));
        assertEquals(new BigDecimal("131.76"), total);
    }

    @Test
    void computeTotalEmptyLinesReturnsZero() {
        BigDecimal total = OrderPricingCalculator.computeTotal(List.of(), new BigDecimal("0.10"));
        assertEquals(new BigDecimal("0.00"), total);
    }
}
