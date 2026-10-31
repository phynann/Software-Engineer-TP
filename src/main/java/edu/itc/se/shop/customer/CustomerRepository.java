package edu.itc.se.shop.customer;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepository {
    private final JdbcClient jdbc;

    public CustomerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Customer> findAll() {
        return jdbc.sql("SELECT id, name, email FROM customer ORDER BY id")
                .query(Customer.class)
                .list();
    }

    public Optional<Customer> findById(long id) {
        return jdbc.sql("SELECT id, name, email FROM customer WHERE id = :id")
                .param("id", id)
                .query(Customer.class)
                .optional();
    }

    public long insert(Customer c) {
        var keys = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO customer (name, email) VALUES (:name, :email)")
                .param("name", c.name())
                .param("email", c.email())
                .update(keys, "id");
        return keys.getKey().longValue();
    }
}
