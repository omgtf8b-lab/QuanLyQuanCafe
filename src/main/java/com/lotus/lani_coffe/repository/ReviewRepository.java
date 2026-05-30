package com.lotus.lani_coffe.repository;

import com.lotus.lani_coffe.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, String status);
    List<Review> findAllByOrderByCreatedAtDesc();
}
