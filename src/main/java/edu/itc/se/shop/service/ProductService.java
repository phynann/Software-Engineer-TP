package edu.itc.se.shop.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.itc.se.shop.domain.Product;
import edu.itc.se.shop.repository.ProductRepository;
import edu.itc.se.shop.web.dto.CreateProduct;
import edu.itc.se.shop.web.dto.ProductDto;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<ProductDto> search(String q) {
        List<Product> products = (q == null || q.isBlank())
                ? repo.findAll()
                : repo.findByNameContaining(q);
        return products.stream().map(ProductDto::from).toList();
    }

    public ProductDto get(long id) {
        return repo.findById(id)
                .map(ProductDto::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public ProductDto create(CreateProduct cmd) {
        Product p = new Product(null, cmd.name(), cmd.price(), cmd.stock());
        long id = repo.insert(p);
        return new ProductDto(id, cmd.name(), cmd.price(), cmd.stock());
    }

    @Transactional
    public ProductDto updateStock(long id, int delta) {
        Product current = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (current.stock() + delta < 0) {
            throw new InsufficientStockException("Product " + id + " has " + current.stock() + " units, cannot remove " + (-delta));
        }
        int rows = repo.updateStock(id, delta);
        if (rows == 0) {
            throw new InsufficientStockException("Product " + id + " has " + current.stock() + " units, cannot remove " + (-delta));
        }
        return repo.findById(id)
                .map(ProductDto::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public void delete(long id) {
        int rows = repo.delete(id);
        if (rows == 0) {
            throw new ProductNotFoundException(id);
        }
    }

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
