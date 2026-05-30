package com.lotus.lani_coffe;

import com.lotus.lani_coffe.model.Category;
import com.lotus.lani_coffe.model.Product;
import com.lotus.lani_coffe.model.User;
import com.lotus.lani_coffe.repository.CategoryRepository;
import com.lotus.lani_coffe.repository.ProductRepository;
import com.lotus.lani_coffe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.lotus.lani_coffe.repository.CouponRepository couponRepository;

    @Override
    public void run(String... args) throws Exception {
        // Tự động chèn tài khoản mặc định nếu chưa tồn tại
        if (userRepository.count() == 0) {
            seedUsers();
        }

        // Tự động chèn danh mục mặc định nếu chưa tồn tại
        if (categoryRepository.count() == 0) {
            seedCategories();
        }

        // Tự động chèn sản phẩm mặc định nếu chưa tồn tại
        if (productRepository.count() == 0) {
            seedProducts();
        }

        // Tự động chèn khuyến mãi nếu chưa tồn tại
        if (couponRepository.count() == 0) {
            seedCoupons();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void seedUsers() {
        // Hashing "123456" làm mật khẩu mặc định cho cả 2 tài khoản mẫu
        String defaultHashed = hashPassword("123456");

        User admin = User.builder()
                .username("admin")
                .email("admin@lanicoffee.vn")
                .password(defaultHashed)
                .phone("0987654321")
                .role("admin")
                .active(true)
                .build();
        userRepository.save(admin);

        User staff = User.builder()
                .username("staff")
                .email("staff@lanicoffee.vn")
                .password(defaultHashed)
                .phone("0912345678")
                .role("staff")
                .active(true)
                .build();
        userRepository.save(staff);

        User customer = User.builder()
                .username("customer")
                .email("customer@gmail.com")
                .password(defaultHashed)
                .phone("0123456789")
                .role("customer")
                .active(true)
                .build();
        userRepository.save(customer);
    }

    private void seedCategories() {
        Category caPhe = Category.builder()
                .name("Cà Phê Pha Máy")
                .slug("ca-phe-pha-may")
                .icon("ri-cup-line")
                .description("Cà phê pha máy chuẩn gu, thơm ngon đậm đà.")
                .build();
        categoryRepository.save(caPhe);

        Category traTraiCay = Category.builder()
                .name("Trà Trái Cây")
                .slug("tra-trai-cay")
                .icon("ri-leaf-line")
                .description("Trà trái cây tươi mát, thanh nhiệt giải khát.")
                .build();
        categoryRepository.save(traTraiCay);
    }

    private void seedProducts() {
        // Lấy ID danh mục vừa chèn
        Long caPheId = categoryRepository.findBySlug("ca-phe-pha-may").map(Category::getId).orElse(1L);
        Long traTraiCayId = categoryRepository.findBySlug("tra-trai-cay").map(Category::getId).orElse(2L);

        // Cà phê
        Product p1 = Product.builder()
                .name("Cà Phê Sữa Đá")
                .description("Cà phê espresso kết hợp với sữa đặc mang lại hương vị đậm đà truyền thống.")
                .price(35000.0)
                .salePrice(30000.0)
                .stock(100)
                .brand("Lani Coffee")
                .categoryId(caPheId)
                .available(true)
                .build();
        productRepository.save(p1);

        Product p2 = Product.builder()
                .name("Bạc Xỉu")
                .description("Hương vị cà phê nhẹ nhàng hòa quyện cùng vị béo ngậy của sữa tươi và sữa đặc.")
                .price(39000.0)
                .salePrice(null)
                .stock(100)
                .brand("Lani Coffee")
                .categoryId(caPheId)
                .available(true)
                .build();
        productRepository.save(p2);

        Product p3 = Product.builder()
                .name("Cà Phê Đen Đá")
                .description("Cà phê đen nguyên chất pha máy, hương vị mạnh mẽ cho sự khởi đầu ngày mới.")
                .price(29000.0)
                .salePrice(25000.0)
                .stock(100)
                .brand("Lani Coffee")
                .categoryId(caPheId)
                .available(true)
                .build();
        productRepository.save(p3);

        // Trà trái cây
        Product p4 = Product.builder()
                .name("Trà Đào Cam Sả")
                .description("Sự kết hợp hoàn hảo giữa vị ngọt của đào, chua thanh của cam và mùi thơm lừng của sả.")
                .price(45000.0)
                .salePrice(39000.0)
                .stock(100)
                .brand("Lani Coffee")
                .categoryId(traTraiCayId)
                .available(true)
                .build();
        productRepository.save(p4);

        Product p5 = Product.builder()
                .name("Trà Vải Nhiệt Đới")
                .description("Vị trà đen đậm đà kết hợp cùng những quả vải tươi mọng nước mát lạnh.")
                .price(45000.0)
                .salePrice(null)
                .stock(100)
                .brand("Lani Coffee")
                .categoryId(traTraiCayId)
                .available(true)
                .build();
        productRepository.save(p5);
    }

    private void seedCoupons() {
        couponRepository.save(com.lotus.lani_coffe.model.Coupon.builder()
                .code("LANI10")
                .discountValue(10.0)
                .type("PERCENTAGE")
                .active(true)
                .build());

        couponRepository.save(com.lotus.lani_coffe.model.Coupon.builder()
                .code("LANICOFFEE")
                .discountValue(20.0)
                .type("PERCENTAGE")
                .active(true)
                .build());

        couponRepository.save(com.lotus.lani_coffe.model.Coupon.builder()
                .code("FREESHIP")
                .discountValue(5.0)
                .type("PERCENTAGE")
                .active(true)
                .build());

        // Nghiệp vụ đặc thù: Thiết lập điều kiện đơn hàng tối thiểu
        couponRepository.save(com.lotus.lani_coffe.model.Coupon.builder()
                .code("LANISPECIAL")
                .discountValue(30000.0) // Giảm 30.000 đ
                .type("FIXED_AMOUNT")
                .minOrderValue(500000.0) // Đơn hàng từ 500.000 đ trở lên
                .active(true)
                .build());
    }
}
