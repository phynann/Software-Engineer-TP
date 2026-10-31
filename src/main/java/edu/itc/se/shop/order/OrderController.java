package edu.itc.se.shop.order;

import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.itc.se.shop.order.dto.OrderDto;
import edu.itc.se.shop.order.dto.PageResponse;
import edu.itc.se.shop.order.dto.PlaceOrder;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDto> place(@RequestBody @Valid PlaceOrder cmd) {
        OrderDto created = orderService.place(cmd);
        return ResponseEntity.created(URI.create("/api/orders/" + created.id())).body(created);
    }

    @GetMapping
    public PageResponse<OrderDto> list(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return orderService.list(customerId, status, page, size);
    }

    @GetMapping("/{id}")
    public OrderDto get(@PathVariable long id) {
        return orderService.get(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderDto cancel(@PathVariable long id) {
        return orderService.cancel(id);
    }
}
