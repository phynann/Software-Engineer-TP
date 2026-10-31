package edu.itc.se.shop.order;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(
    Long id,
    Long customerId,
    OrderStatus status,
    Instant placedAt,
    BigDecimal total
) {}
