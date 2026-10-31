package edu.itc.se.shop.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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
import edu.itc.se.shop.config.ShopProperties;
import edu.itc.se.shop.service.ProductService;
import edu.itc.se.shop.web.dto.CreateProduct;
import edu.itc.se.shop.web.dto.ProductDto;

@Controller
public class ProductPageController {
    private static final Logger log = LoggerFactory.getLogger(ProductPageController.class);

    private final ProductService productService;
    private final ShopProperties shopProperties;

    public ProductPageController(ProductService productService, ShopProperties shopProperties) {
        this.productService = productService;
        this.shopProperties = shopProperties;
    }

    public record ProductViewItem(long id, String name, BigDecimal price, BigDecimal gross, int stock) {}

    @GetMapping("/products")
    public String list(@RequestParam(required = false) String q, Model model) {
        List<ProductDto> dtos = productService.search(q);
        BigDecimal taxRate = shopProperties.taxRate() != null ? shopProperties.taxRate() : BigDecimal.ZERO;

        List<ProductViewItem> items = dtos.stream().map(dto -> {
            BigDecimal gross = dto.price().multiply(BigDecimal.ONE.add(taxRate)).setScale(2, RoundingMode.HALF_UP);
            return new ProductViewItem(dto.id(), dto.name(), dto.price(), gross, dto.stock());
        }).toList();

        model.addAttribute("products", items);
        model.addAttribute("q", q != null ? q : "");
        model.addAttribute("currency", shopProperties.currency());
        model.addAttribute("threshold", shopProperties.lowStockThreshold());
        return "products/list";
    }

    @GetMapping("/products/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("createProduct")) {
            model.addAttribute("createProduct", new CreateProduct("", BigDecimal.ZERO, 0));
        }
        model.addAttribute("currency", shopProperties.currency());
        return "products/form";
    }

    @PostMapping("/products")
    public String create(@Valid @ModelAttribute("createProduct") CreateProduct form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.warn("failed validation while creating product: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.createProduct", bindingResult);
            redirectAttributes.addFlashAttribute("createProduct", form);
            return "redirect:/products/new";
        }
        ProductDto created = productService.create(form);
        log.info("created product {} '{}'", created.id(), created.name());
        redirectAttributes.addFlashAttribute("message", "Product created successfully");
        return "redirect:/products";
    }
}
