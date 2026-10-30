package edu.itc.se.shop.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import edu.itc.se.shop.repository.ProductRepository;

@SpringBootTest
class ProductServiceTest {
    private final ProductService service;
    private final ProductRepository repo;

    @Autowired
    ProductServiceTest(ProductService service, ProductRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @Test
    void restockRollsBackWhenSourceHasTooLittleStock() {
        int before1 = repo.findById(1).orElseThrow().stock();
        int before2 = repo.findById(2).orElseThrow().stock();

        assertThrows(IllegalStateException.class, () -> service.restock(1, 2, 1_000));

        assertEquals(before1, repo.findById(1).orElseThrow().stock());
        assertEquals(before2, repo.findById(2).orElseThrow().stock());
    }

    @Test
    void restockRollsBackFirstUpdateWhenTargetMissing() {
        int before1 = repo.findById(1).orElseThrow().stock();

        assertThrows(IllegalStateException.class, () -> service.restock(1, 999, 1));

        assertEquals(before1, repo.findById(1).orElseThrow().stock());
    }
}