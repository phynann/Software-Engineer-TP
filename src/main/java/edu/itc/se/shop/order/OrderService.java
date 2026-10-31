package edu.itc.se.shop.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.itc.se.shop.audit.Audited;
import edu.itc.se.shop.config.ShopProperties;
import edu.itc.se.shop.customer.CustomerRepository;
import edu.itc.se.shop.domain.Product;
import edu.itc.se.shop.order.dto.OrderDto;
import edu.itc.se.shop.order.dto.OrderLineDto;
import edu.itc.se.shop.order.dto.PageResponse;
import edu.itc.se.shop.order.dto.PlaceOrder;
import edu.itc.se.shop.repository.ProductRepository;
import edu.itc.se.shop.service.InsufficientStockException;
import edu.itc.se.shop.service.Notifier;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final ShopProperties props;
    private final Notifier notifier;

    public OrderService(OrderRepository orders,
                        ProductRepository products,
                        CustomerRepository customers,
                        ShopProperties props,
                        Notifier notifier) {
        this.orders = orders;
        this.products = products;
        this.customers = customers;
        this.props = props;
        this.notifier = notifier;
    }

    @Transactional
    @Audited
    public OrderDto place(PlaceOrder cmd) {
        if (cmd.lines() == null || cmd.lines().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line");
        }

        Set<Long> seenProducts = new HashSet<>();
        for (PlaceOrder.Line line : cmd.lines()) {
            if (!seenProducts.add(line.productId())) {
                throw new IllegalArgumentException("Duplicate product in order lines: " + line.productId());
            }
        }

        customers.findById(cmd.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer " + cmd.customerId() + " not found"));

        List<OrderPricingCalculator.LinePrice> linePrices = new ArrayList<>();
        List<OrderLine> orderLines = new ArrayList<>();

        for (PlaceOrder.Line line : cmd.lines()) {
            Product product = products.findById(line.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product " + line.productId() + " not found"));

            int updated = products.updateStock(line.productId(), -line.qty());
            if (updated == 0) {
                throw new InsufficientStockException("Insufficient stock for product " + line.productId());
            }

            BigDecimal unitPrice = product.price();
            linePrices.add(new OrderPricingCalculator.LinePrice(line.qty(), unitPrice));
            orderLines.add(new OrderLine(null, null, line.productId(), line.qty(), unitPrice));
        }

        BigDecimal total = OrderPricingCalculator.computeTotal(linePrices, props.taxRate());
        Order order = new Order(null, cmd.customerId(), OrderStatus.NEW, Instant.now(), total);
        long orderId = orders.insert(order, orderLines);

        List<OrderLineDto> lineDtos = orderLines.stream()
                .map(l -> new OrderLineDto(l.productId(), l.qty(), l.unitPrice()))
                .toList();

        return new OrderDto(orderId, cmd.customerId(), OrderStatus.NEW, order.placedAt(), total, lineDtos);
    }

    @Transactional
    @Audited
    public OrderDto cancel(long id) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.status() != OrderStatus.NEW) {
            throw new IllegalStateException("Only NEW orders can be cancelled, current status is " + order.status());
        }

        orders.updateStatus(id, OrderStatus.CANCELLED);

        List<OrderLine> lines = orders.findLinesByOrderId(id);
        for (OrderLine line : lines) {
            products.updateStock(line.productId(), line.qty());
        }

        notifier.send(String.valueOf(order.customerId()), "Order " + id + " has been cancelled.");

        List<OrderLineDto> lineDtos = lines.stream()
                .map(l -> new OrderLineDto(l.productId(), l.qty(), l.unitPrice()))
                .toList();

        return new OrderDto(order.id(), order.customerId(), OrderStatus.CANCELLED, order.placedAt(), order.total(), lineDtos);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderDto> list(Long customerId, OrderStatus status, int page, int size) {
        int pageNumber = Math.max(0, page);
        int pageSize = (size > 0) ? size : 10;
        int offset = pageNumber * pageSize;

        List<Order> orderList = orders.findOrders(customerId, status, offset, pageSize);
        long totalCount = orders.countOrders(customerId, status);

        List<OrderDto> dtos = orderList.stream().map(o -> {
            List<OrderLineDto> lines = orders.findLinesByOrderId(o.id()).stream()
                    .map(l -> new OrderLineDto(l.productId(), l.qty(), l.unitPrice()))
                    .toList();
            return new OrderDto(o.id(), o.customerId(), o.status(), o.placedAt(), o.total(), lines);
        }).toList();

        return new PageResponse<>(dtos, pageNumber, pageSize, totalCount);
    }

    @Transactional(readOnly = true)
    public OrderDto get(long id) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        List<OrderLineDto> lineDtos = orders.findLinesByOrderId(id).stream()
                .map(l -> new OrderLineDto(l.productId(), l.qty(), l.unitPrice()))
                .toList();

        return new OrderDto(order.id(), order.customerId(), order.status(), order.placedAt(), order.total(), lineDtos);
    }
}
