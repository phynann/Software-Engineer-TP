package edu.itc.se.shop.order.dto;

import java.math.BigDecimal;

public record OrderLineDto(Long productId, int qty, BigDecimal unitPrice) {}
