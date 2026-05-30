package com.lotus.lani_coffe.controller;

import com.lotus.lani_coffe.model.Review;
import com.lotus.lani_coffe.model.User;
import com.lotus.lani_coffe.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    @Autowired
    private ReviewService reviewService;

    // Lấy đánh giá đã duyệt của 1 sản phẩm cụ thể
    @GetMapping("/product/{productId}")
    public List<Review> getApprovedReviewsByProduct(@PathVariable Long productId) {
        return reviewService.getApprovedReviewsByProduct(productId);
    }

    // Đăng bình luận, đánh giá mới (Khách hàng)
    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody Review review, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập để đánh giá hạt cà phê!");
        }

        try {
            review.setUserId(currentUser.getId());
            Review saved = reviewService.submitReview(review);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi gửi đánh giá: " + e.getMessage());
        }
    }

    // Lấy tất cả đánh giá để Admin duyệt
    @GetMapping
    public ResponseEntity<?> getAllReviews(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền truy cập!");
        }
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // Phê duyệt bình luận (Admin)
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveReview(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền phê duyệt!");
        }
        try {
            Review approved = reviewService.approveReview(id);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa bình luận (Admin hoặc người viết bình luận)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền thực hiện!");
        }
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
