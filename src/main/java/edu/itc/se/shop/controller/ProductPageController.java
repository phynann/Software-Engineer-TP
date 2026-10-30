package edu.itc.se.shop.controller;

import edu.itc.se.shop.config.ShopProperties;
import edu.itc.se.shop.model.Product;
import edu.itc.se.shop.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProductPageController {
    private static final Logger log = LoggerFactory.getLogger(ProductPageController.class);

    private final ProductService productService;
    private final ShopProperties shopProperties;

    public ProductPageController(ProductService productService, ShopProperties shopProperties) {
        this.productService = productService;
        this.shopProperties = shopProperties;
    }

    @GetMapping("/products")
    public String list(@RequestParam(value = "q", required = false) String q, Model model) {
        List<ProductView> products = productService.findAll(q).stream()
                .map(p -> toView(p))
                .collect(Collectors.toList());
        model.addAttribute("products", products);
        model.addAttribute("q", q);
        model.addAttribute("currency", shopProperties.currency());
        model.addAttribute("threshold", shopProperties.lowStockThreshold());
        return "products/list";
    }

    @GetMapping("/products/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("product")) {
            model.addAttribute("product", new Product());
        }
        model.addAttribute("currency", shopProperties.currency());
        return "products/form";
    }

    @PostMapping("/products")
    public String create(@Valid @ModelAttribute("product") Product product,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.warn("failed validation while creating product");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.product", bindingResult);
            redirectAttributes.addFlashAttribute("product", product);
            return "redirect:/products/new";
        }
        productService.create(product);
        redirectAttributes.addFlashAttribute("message", "Product created");
        return "redirect:/products";
    }

    private ProductView toView(Product product) {
        BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        BigDecimal taxRate = shopProperties.taxRate() == null ? BigDecimal.ZERO : shopProperties.taxRate();
        BigDecimal gross = price.multiply(BigDecimal.ONE.add(taxRate)).setScale(2, RoundingMode.HALF_UP);
        return new ProductView(product.getName(), price, gross, product.getStock());
    }

    public static class ProductView {
        private final String name;
        private final BigDecimal price;
        private final BigDecimal gross;
        private final Integer stock;

        public ProductView(String name, BigDecimal price, BigDecimal gross, Integer stock) {
            this.name = name;
            this.price = price;
            this.gross = gross;
            this.stock = stock;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public BigDecimal getGross() {
            return gross;
        }

        public Integer getStock() {
            return stock;
        }
    }
}
