package com.lotus.lani_coffe.controller;

import com.lotus.lani_coffe.model.Coupon;
import com.lotus.lani_coffe.model.User;
import com.lotus.lani_coffe.service.CouponService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponApiController {

    @Autowired
    private CouponService couponService;

    // Lấy danh sách tất cả các coupon (Admin)
    @GetMapping
    public ResponseEntity<?> getAllCoupons(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền truy cập!");
        }
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    // Tạo mới/cập nhật coupon (Admin)
    @PostMapping
    public ResponseEntity<?> saveCoupon(@RequestBody Coupon coupon, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền thực hiện!");
        }
        try {
            Coupon saved = couponService.saveCoupon(coupon);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa coupon (Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền thực hiện!");
        }
        couponService.deleteCoupon(id);
        return ResponseEntity.ok("Đã xóa mã giảm giá thành công!");
    }
}
