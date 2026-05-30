package com.lotus.lani_coffe.service;

import com.lotus.lani_coffe.model.Coupon;
import com.lotus.lani_coffe.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public Optional<Coupon> getCouponById(Long id) {
        return couponRepository.findById(id);
    }

    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code.trim());
    }

    public Coupon saveCoupon(Coupon coupon) {
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã giảm giá không được để trống!");
        }
        if (coupon.getDiscountValue() == null || coupon.getDiscountValue() <= 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phải lớn hơn 0!");
        }
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getType()) && coupon.getDiscountValue() > 100) {
            throw new IllegalArgumentException("Giá trị giảm theo phần trăm không được vượt quá 100%!");
        }
        return couponRepository.save(coupon);
    }

    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }

    // Nghiệp vụ kiểm tra tính hợp lệ của coupon
    public Optional<Coupon> validateCoupon(String code, Double orderValue) {
        Optional<Coupon> couponOpt = getCouponByCode(code);
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            LocalDateTime now = LocalDateTime.now();

            if (!coupon.getActive()) {
                return Optional.empty();
            }
            if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(now)) {
                return Optional.empty();
            }
            if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(now)) {
                return Optional.empty();
            }
            // Nghiệp vụ 2: Thiết lập điều kiện áp dụng (Ví dụ: Đơn trên 500k mới được dùng mã)
            if (coupon.getMinOrderValue() != null && orderValue < coupon.getMinOrderValue()) {
                throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu " + coupon.getMinOrderValue() + " đ để áp dụng mã này!");
            }
            return Optional.of(coupon);
        }
        return Optional.empty();
    }
}
