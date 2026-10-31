package edu.itc.se.shop.health;

import java.util.List;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import edu.itc.se.shop.config.ShopProperties;
import edu.itc.se.shop.domain.Product;
import edu.itc.se.shop.repository.ProductRepository;

@Component("lowStock")
public class LowStockHealthIndicator implements HealthIndicator {
    private final ProductRepository productRepo;
    private final ShopProperties props;

    public LowStockHealthIndicator(ProductRepository productRepo, ShopProperties props) {
        this.productRepo = productRepo;
        this.props = props;
    }

    @Override
    public Health health() {
        List<String> lowStockProducts = productRepo.findAll().stream()
                .filter(p -> p.stock() < props.lowStockThreshold())
                .map(Product::name)
                .toList();

        if (!lowStockProducts.isEmpty()) {
            return Health.down()
                    .withDetail("lowStockProducts", lowStockProducts)
                    .build();
        }
        return Health.up().build();
    }
}
