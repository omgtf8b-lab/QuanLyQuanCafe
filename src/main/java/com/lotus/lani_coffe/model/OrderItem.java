package com.lotus.lani_coffe.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Mã đơn hàng không được để trống")
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Order order;

    @NotNull(message = "Mã sản phẩm không được để trống")
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Product product;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @PositiveOrZero(message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    @Column(nullable = false)
    private Double price;

    @NotNull(message = "Số lượng sản phẩm không được để trống")
    @Min(value = 1, message = "Số lượng sản phẩm tối thiểu phải là 1")
    @Column(nullable = false)
    private Integer quantity;
}
