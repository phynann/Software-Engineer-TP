package edu.itc.se.shop.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import edu.itc.se.shop.domain.Product;
import edu.itc.se.shop.repository.ProductRepository;

@JdbcTest
@Import(ProductRepository.class)
class ProductRepositoryTest {
    private final ProductRepository repo;

    @Autowired
    ProductRepositoryTest(ProductRepository repo) { this.repo = repo; }

    @Test
    void findAllReturnsSeedData() {
        assertEquals(5, repo.findAll().size());
    }

    @Test
    void findByIdFoundAndMissing() {
        assertEquals("MILK", repo.findById(1).orElseThrow().name());
        assertTrue(repo.findById(999).isEmpty());
    }

    @Test
    void findByNameContaining() {
        assertEquals(1, repo.findByNameContaining("ham").size());
    }

    @Test
    void insertReturnsGeneratedId() {
        long id = repo.insert(new Product(null, "BREAD", new BigDecimal("2.50"), 30));
        assertTrue(id > 0);
        assertEquals("BREAD", repo.findById(id).orElseThrow().name());
    }

    @Test
    void updateStockRespectsNonNegativeGuard() {
        assertEquals(1, repo.updateStock(1, 5));
        assertEquals(0, repo.updateStock(1, -10_000));
    }

    @Test
    void deleteRemovesRow() {
        assertEquals(1, repo.delete(5));
        assertTrue(repo.findById(5).isEmpty());
    }

    @Test
    void duplicateNameThrowsDuplicateKeyException() {
        var dup = new Product(null, "MILK", new BigDecimal("1.00"), 1);
        var ex = assertThrows(DuplicateKeyException.class, () -> repo.insert(dup));
        System.out.println("Exception: " + ex.getClass() + " / cause: " + ex.getCause().getClass());
    }
}