package edu.itc.se.shop.order;

import java.math.BigDecimal;

public record OrderLine(
    Long id,
    Long orderId,
    Long productId,
    int qty,
    BigDecimal unitPrice
) {}
