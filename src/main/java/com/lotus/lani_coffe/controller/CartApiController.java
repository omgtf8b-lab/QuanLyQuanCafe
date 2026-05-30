package com.lotus.lani_coffe.controller;

import com.lotus.lani_coffe.model.User;
import com.lotus.lani_coffe.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    // Lấy giỏ hàng của user
    @GetMapping
    public ResponseEntity<?> getCart(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        return ResponseEntity.ok(cartService.getCartItemsByUser(user.getId()));
    }

    // Lấy tổng số lượng sản phẩm trong giỏ hàng (cho Navbar Badge)
    @GetMapping("/count")
    public ResponseEntity<?> getCartCount(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.ok(0); // Trả về 0 nếu chưa đăng nhập
        }
        int count = cartService.getCartItemsByUser(user.getId()).stream()
                .mapToInt(item -> item.getQuantity())
                .sum();
        return ResponseEntity.ok(count);
    }

    // Thêm sản phẩm vào giỏ
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Integer> payload, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        
        Integer productIdInt = payload.get("productId");
        Integer quantity = payload.getOrDefault("quantity", 1);
        
        if (productIdInt == null) {
            return ResponseEntity.badRequest().body("Thiếu productId");
        }
        
        try {
            return ResponseEntity.ok(cartService.addToCart(user.getId(), productIdInt.longValue(), quantity));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật số lượng
    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestBody Map<String, Integer> payload, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }

        Integer productIdInt = payload.get("productId");
        Integer quantity = payload.get("quantity");

        if (productIdInt == null || quantity == null) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ");
        }

        try {
            cartService.updateCartItemQuantity(user.getId(), productIdInt.longValue(), quantity);
            return ResponseEntity.ok("Đã cập nhật số lượng");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa sản phẩm khỏi giỏ
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        cartService.removeFromCart(user.getId(), productId);
        return ResponseEntity.ok("Đã xóa khỏi giỏ hàng");
    }

    // Làm trống giỏ hàng
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        cartService.clearCart(user.getId());
        return ResponseEntity.ok("Đã làm trống giỏ hàng");
    }
}
