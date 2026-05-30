package com.lotus.lani_coffe.controller;

import com.lotus.lani_coffe.model.Product;
import com.lotus.lani_coffe.model.ProductImage;
import com.lotus.lani_coffe.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @GetMapping("/brands")
    public List<String> getAllBrands() {
        return productService.getAllBrands();
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String q) {
        return productService.searchProducts(q);
    }

    @PostMapping
    public Product createProduct(@Valid @RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        return productService.getProductById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setSalePrice(productDetailsDetailsPrice(productDetails));
            product.setStock(productDetails.getStock());
            product.setBrand(productDetails.getBrand());
            product.setCategoryId(productDetails.getCategoryId());
            product.setAvailable(productDetails.getAvailable());
            return ResponseEntity.ok(productService.saveProduct(product));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Double productDetailsDetailsPrice(Product productDetails) {
        return productDetails.getSalePrice();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return productService.getProductById(id).map(product -> {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // Lấy danh sách ảnh phụ
    @GetMapping("/{id}/images")
    public List<ProductImage> getProductImages(@PathVariable Long id) {
        return productService.getProductImages(id);
    }

    // Lưu bộ sưu tập ảnh phụ
    @PostMapping("/{id}/images")
    public List<ProductImage> saveProductImages(@PathVariable Long id, @RequestBody List<String> imageUrls) {
        productService.clearProductImages(id);
        for (String url : imageUrls) {
            productService.addProductImage(id, url);
        }
        return productService.getProductImages(id);
    }

    // Nghiệp vụ 3: Upload ảnh vật lý
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String relativeUrl = productService.saveUploadedFile(file);
            if (relativeUrl == null) {
                return ResponseEntity.badRequest().body("File rỗng!");
            }
            Map<String, String> response = new HashMap<>();
            response.put("url", relativeUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi tải ảnh lên: " + e.getMessage());
        }
    }
}
