package edu.itc.se.shop.order;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    private final JdbcClient jdbc;

    private final RowMapper<Order> orderMapper = (rs, rowNum) -> new Order(
            rs.getLong("id"),
            rs.getLong("customer_id"),
            OrderStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("placed_at").toInstant(),
            rs.getBigDecimal("total")
    );

    private final RowMapper<OrderLine> lineMapper = (rs, rowNum) -> new OrderLine(
            rs.getLong("id"),
            rs.getLong("order_id"),
            rs.getLong("product_id"),
            rs.getInt("qty"),
            rs.getBigDecimal("unit_price")
    );

    public OrderRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(Order order, List<OrderLine> lines) {
        var keys = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO orders (customer_id, status, placed_at, total) VALUES (:customerId, :status, :placedAt, :total)")
                .param("customerId", order.customerId())
                .param("status", order.status().name())
                .param("placedAt", Timestamp.from(order.placedAt()))
                .param("total", order.total())
                .update(keys, "id");
        long orderId = keys.getKey().longValue();

        for (OrderLine line : lines) {
            jdbc.sql("INSERT INTO order_line (order_id, product_id, qty, unit_price) VALUES (:orderId, :productId, :qty, :unitPrice)")
                    .param("orderId", orderId)
                    .param("productId", line.productId())
                    .param("qty", line.qty())
                    .param("unitPrice", line.unitPrice())
                    .update();
        }

        return orderId;
    }

    public Optional<Order> findById(long id) {
        return jdbc.sql("SELECT id, customer_id, status, placed_at, total FROM orders WHERE id = :id")
                .param("id", id)
                .query(orderMapper)
                .optional();
    }

    public List<OrderLine> findLinesByOrderId(long orderId) {
        return jdbc.sql("SELECT id, order_id, product_id, qty, unit_price FROM order_line WHERE order_id = :orderId ORDER BY id")
                .param("orderId", orderId)
                .query(lineMapper)
                .list();
    }

    public int updateStatus(long orderId, OrderStatus status) {
        return jdbc.sql("UPDATE orders SET status = :status WHERE id = :id")
                .param("status", status.name())
                .param("id", orderId)
                .update();
    }

    public List<Order> findOrders(Long customerId, OrderStatus status, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id, customer_id, status, placed_at, total FROM orders WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (customerId != null) {
            sql.append(" AND customer_id = ?");
            params.add(customerId);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        var query = jdbc.sql(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            query.param(i + 1, params.get(i));
        }
        return query.query(orderMapper).list();
    }

    public long countOrders(Long customerId, OrderStatus status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (customerId != null) {
            sql.append(" AND customer_id = ?");
            params.add(customerId);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }

        var query = jdbc.sql(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            query.param(i + 1, params.get(i));
        }
        Long count = query.query(Long.class).single();
        return count != null ? count : 0L;
    }
}
