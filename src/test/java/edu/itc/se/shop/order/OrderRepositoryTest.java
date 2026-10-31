package edu.itc.se.shop.order;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import edu.itc.se.shop.customer.Customer;
import edu.itc.se.shop.customer.CustomerRepository;

@JdbcTest
@Import({OrderRepository.class, CustomerRepository.class})
class OrderRepositoryTest {

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;

    @Autowired
    OrderRepositoryTest(OrderRepository orderRepo, CustomerRepository customerRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
    }

    @Test
    void insertAndFindOrderWithLines() {
        long custId = customerRepo.insert(new Customer(null, "Charlie", "charlie@example.com"));

        Order order = new Order(null, custId, OrderStatus.NEW, Instant.now(), new BigDecimal("55.00"));
        List<OrderLine> lines = List.of(
                new OrderLine(null, null, 1L, 2, new BigDecimal("20.00")),
                new OrderLine(null, null, 2L, 1, new BigDecimal("12.00"))
        );

        long orderId = orderRepo.insert(order, lines);
        assertTrue(orderId > 0);

        Order found = orderRepo.findById(orderId).orElseThrow();
        assertEquals(custId, found.customerId());
        assertEquals(OrderStatus.NEW, found.status());
        assertEquals(new BigDecimal("55.00"), found.total());

        List<OrderLine> foundLines = orderRepo.findLinesByOrderId(orderId);
        assertEquals(2, foundLines.size());
    }

    @Test
    void updateStatusChangesOrderStatus() {
        Order order = new Order(null, 1L, OrderStatus.NEW, Instant.now(), new BigDecimal("20.00"));
        long orderId = orderRepo.insert(order, List.of(new OrderLine(null, null, 1L, 1, new BigDecimal("20.00"))));

        int rows = orderRepo.updateStatus(orderId, OrderStatus.CANCELLED);
        assertEquals(1, rows);

        Order updated = orderRepo.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, updated.status());
    }

    @Test
    void findOrdersWithPagination() {
        for (int i = 0; i < 5; i++) {
            Order order = new Order(null, 1L, OrderStatus.NEW, Instant.now(), new BigDecimal("10.00"));
            orderRepo.insert(order, List.of(new OrderLine(null, null, 1L, 1, new BigDecimal("10.00"))));
        }

        List<Order> page0 = orderRepo.findOrders(1L, OrderStatus.NEW, 0, 3);
        assertEquals(3, page0.size());

        List<Order> page1 = orderRepo.findOrders(1L, OrderStatus.NEW, 3, 3);
        assertTrue(page1.size() >= 2);

        long count = orderRepo.countOrders(1L, OrderStatus.NEW);
        assertTrue(count >= 5);
    }
}
