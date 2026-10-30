package edu.itc.se.shop.service;

import edu.itc.se.shop.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final List<Product> products = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong idCounter = new AtomicLong(1);

    public ProductService() {
        addInitialProduct("Keyboard", new BigDecimal("49.99"), 2);
        addInitialProduct("Mouse", new BigDecimal("19.99"), 50);
        addInitialProduct("USB-C cable", new BigDecimal("9.99"), 100);
    }

    private void addInitialProduct(String name, BigDecimal price, int stock) {
        Product product = new Product(idCounter.getAndIncrement(), name, price, stock);
        products.add(product);
    }

    public List<Product> findAll(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(products);
        }
        String q = query.toLowerCase();
        return products.stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Product> findAll() {
        return findAll(null);
    }

    public Product create(Product product) {
        product.setId(idCounter.getAndIncrement());
        products.add(product);
        log.info("created product {} '{}'", product.getId(), product.getName());
        return product;
    }

    public List<Product> findLowStock(int threshold) {
        return products.stream()
                .filter(p -> p.getStock() != null && p.getStock() < threshold)
                .collect(Collectors.toList());
    }
}
