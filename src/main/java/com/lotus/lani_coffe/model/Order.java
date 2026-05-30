package com.lotus.lani_coffe.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId; // Liên kết với tài khoản đặt hàng, có thể null nếu mua không cần đăng nhập

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 255, message = "Tên khách hàng không được vượt quá 255 ký tự")
    @Column(name = "customer_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0")
    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Column(name = "shipping_address", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String shippingAddress;

    @NotNull(message = "Tổng tiền trước giảm giá không được để trống")
    @Column(nullable = false)
    private Double subtotal; // Tổng tiền trước giảm giá

    @Column(nullable = false)
    private Double discount = 0.0; // Tiền giảm giá từ coupon

    @Column(nullable = false)
    private Double tax = 0.0; // Thuế (nếu có)

    @NotNull(message = "Tổng thanh toán không được để trống")
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount; // Tổng thanh toán thực tế

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // "COD" hoặc "TRANSFER"

    @NotBlank(message = "Trạng thái đơn hàng không được để trống")
    @Column(nullable = false)
    private String status; // "PENDING", "CONFIRMED", "SHIPPING", "COMPLETED", "CANCELLED"

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note; // Ghi chú pha chế (Ví dụ: "Ít đường, nhiều đá")

    @Column(name = "coupon_id")
    private Long couponId; // Mã giảm giá được dùng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Coupon coupon;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // Ghi nhận thời điểm hoàn tất giao hàng
}
