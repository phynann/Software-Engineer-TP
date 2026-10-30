package edu.itc.se.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.itc.se.shop.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) { this.repo = repo; }

    @Transactional
    public void restock(long fromID, long toID, int qty) {
        if (repo.updateStock(fromID, -qty) == 0) {
            throw new IllegalStateException("not enough stock: " + fromID);
        }
        if (repo.updateStock(toID, qty) == 0) {
            throw new IllegalStateException("product missing: " + toID);
        }
    }
}