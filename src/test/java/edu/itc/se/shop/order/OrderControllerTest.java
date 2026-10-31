package edu.itc.se.shop.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import edu.itc.se.shop.order.dto.OrderDto;
import edu.itc.se.shop.order.dto.OrderLineDto;
import edu.itc.se.shop.order.dto.PageResponse;
import edu.itc.se.shop.order.dto.PlaceOrder;
import edu.itc.se.shop.web.ApiExceptionHandler;

@WebMvcTest(OrderController.class)
@Import(ApiExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void placeOrderReturns201WithLocation() throws Exception {
        OrderDto created = new OrderDto(
                17L,
                1L,
                OrderStatus.NEW,
                Instant.now(),
                new BigDecimal("131.88"),
                List.of(
                        new OrderLineDto(1L, 2, new BigDecimal("49.90")),
                        new OrderLineDto(3L, 1, new BigDecimal("10.00"))
                )
        );

        when(orderService.place(any(PlaceOrder.class))).thenReturn(created);

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1,
                                  "lines": [
                                    {"productId": 1, "qty": 2},
                                    {"productId": 3, "qty": 1}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/17"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.total").value(131.88));
    }

    @Test
    void placeOrderWithEmptyLinesReturns400() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1,
                                  "lines": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void getOrderReturns200WhenFound() throws Exception {
        OrderDto dto = new OrderDto(
                17L,
                1L,
                OrderStatus.NEW,
                Instant.now(),
                new BigDecimal("131.88"),
                List.of(new OrderLineDto(1L, 2, new BigDecimal("49.90")))
        );

        when(orderService.get(17L)).thenReturn(dto);

        mvc.perform(get("/api/orders/17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.lines.length()").value(1));
    }

    @Test
    void getOrderReturns404WhenNotFound() throws Exception {
        when(orderService.get(999L)).thenThrow(new OrderNotFoundException(999L));

        mvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Order 999 not found"));
    }

    @Test
    void listOrdersReturns200WithPageResponse() throws Exception {
        OrderDto dto = new OrderDto(
                17L, 1L, OrderStatus.NEW, Instant.now(), new BigDecimal("131.88"), List.of()
        );
        when(orderService.list(eq(1L), eq(OrderStatus.NEW), eq(0), eq(10)))
                .thenReturn(new PageResponse<>(List.of(dto), 0, 10, 1));

        mvc.perform(get("/api/orders?customerId=1&status=NEW&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void cancelOrderReturns200OnSuccess() throws Exception {
        OrderDto cancelled = new OrderDto(
                17L, 1L, OrderStatus.CANCELLED, Instant.now(), new BigDecimal("131.88"), List.of()
        );
        when(orderService.cancel(17L)).thenReturn(cancelled);

        mvc.perform(post("/api/orders/17/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelNonNewOrderReturns409() throws Exception {
        when(orderService.cancel(17L)).thenThrow(new IllegalStateException("Only NEW orders can be cancelled"));

        mvc.perform(post("/api/orders/17/cancel"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Conflict"));
    }
}
