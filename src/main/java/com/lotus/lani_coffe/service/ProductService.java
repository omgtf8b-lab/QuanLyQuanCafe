package com.lotus.lani_coffe.service;

import com.lotus.lani_coffe.model.Product;
import com.lotus.lani_coffe.model.ProductImage;
import com.lotus.lani_coffe.repository.ProductImageRepository;
import com.lotus.lani_coffe.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    // Đường dẫn thư mục lưu ảnh upload tại gốc dự án để phục vụ trực tiếp
    private final String UPLOAD_DIR = "uploads";

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<String> getAllBrands() {
        return productRepository.findDistinctBrands();
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    public Product saveProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống!");
        }
        if (product.getPrice() == null || product.getPrice() < 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn hoặc bằng 0!");
        }
        if (product.getSalePrice() != null && product.getSalePrice() < 0) {
            throw new IllegalArgumentException("Giá khuyến mãi phải lớn hơn hoặc bằng 0!");
        }
        if (product.getSalePrice() != null && product.getPrice() != null && product.getSalePrice() > product.getPrice()) {
            throw new IllegalArgumentException("Giá khuyến mãi không được lớn hơn giá gốc!");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho phải lớn hơn hoặc bằng 0!");
        }
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        productImageRepository.deleteByProductId(id);
    }

    // Lấy bộ sưu tập ảnh của sản phẩm
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    // Nghiệp vụ đặc thù 3: API Lưu trữ hình ảnh vật lý lên Server
    public String saveUploadedFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // Tạo thư mục uploads tại gốc dự án nếu chưa tồn tại
        File uploadFolder = new File(UPLOAD_DIR);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        // Sinh tên file ngẫu nhiên để tránh trùng lặp
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        // Ghi file vật lý
        byte[] bytes = file.getBytes();
        Path path = Paths.get(UPLOAD_DIR + File.separator + newFilename);
        Files.write(path, bytes);

        // Trả về đường dẫn tương đối để lưu vào DB
        return "/uploads/" + newFilename;
    }

    @Transactional
    public ProductImage addProductImage(Long productId, String imageUrl) {
        ProductImage img = ProductImage.builder()
                .productId(productId)
                .imageUrl(imageUrl)
                .build();
        return productImageRepository.save(img);
    }

    @Transactional
    public void clearProductImages(Long productId) {
        productImageRepository.deleteByProductId(productId);
    }
}
