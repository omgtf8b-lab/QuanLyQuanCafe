package com.lotus.lani_coffe.service;

import com.lotus.lani_coffe.model.Review;
import com.lotus.lani_coffe.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Review> getApprovedReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, "APPROVED");
    }

    public Review submitReview(Review review) {
        review.setCreatedAt(LocalDateTime.now());
        review.setStatus("PENDING"); // Đánh giá mới luôn ở trạng thái chờ kiểm duyệt
        return reviewRepository.save(review);
    }

    public Review approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá!"));
        review.setStatus("APPROVED");
        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
