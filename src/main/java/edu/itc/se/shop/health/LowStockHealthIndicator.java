package edu.itc.se.shop.health;

import edu.itc.se.shop.config.ShopProperties;
import edu.itc.se.shop.model.Product;
import edu.itc.se.shop.service.ProductService;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component("lowStock")
public class LowStockHealthIndicator implements HealthIndicator {
    private final ProductService productService;
    private final ShopProperties shopProperties;

    public LowStockHealthIndicator(ProductService productService, ShopProperties shopProperties) {
        this.productService = productService;
        this.shopProperties = shopProperties;
    }

    @Override
    public Health health() {
        List<Product> lowStockProducts = productService.findAll().stream()
                .filter(p -> p.getStock() != null && p.getStock() < shopProperties.lowStockThreshold())
                .collect(Collectors.toList());

        if (!lowStockProducts.isEmpty()) {
            List<String> names = lowStockProducts.stream()
                    .map(Product::getName)
                    .collect(Collectors.toList());
            return Health.down()
                    .withDetail("lowStockProducts", names)
                    .build();
        }
        return Health.up().build();
    }
}
