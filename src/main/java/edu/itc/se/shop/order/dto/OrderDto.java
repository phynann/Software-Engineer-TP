package edu.itc.se.shop.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import edu.itc.se.shop.order.OrderStatus;

public record OrderDto(
    Long id,
    Long customerId,
    OrderStatus status,
    Instant placedAt,
    BigDecimal total,
    List<OrderLineDto> lines
) {}
