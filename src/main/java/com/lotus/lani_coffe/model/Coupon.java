package com.lotus.lani_coffe.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // LANI10, LANICOFFEE, FREESHIP...

    @Column(nullable = false)
    private Double discountValue; // Số phần trăm hoặc số tiền giảm

    @Column(nullable = false)
    private String type; // PERCENTAGE hoặc FIXED_AMOUNT

    @Column(name = "min_order_value")
    private Double minOrderValue; // Điều kiện đơn hàng tối thiểu áp dụng (ví dụ đơn > 500k)

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean active = true;
}
