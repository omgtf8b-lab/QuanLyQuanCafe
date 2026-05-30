package com.lotus.lani_coffe.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description; // Nguồn gốc, tỷ lệ hạt (Arabica/Robusta), hương vị...

    @NotNull(message = "Giá sản phẩm không được để trống")
    @PositiveOrZero(message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    @Column(nullable = false)
    private Double price;

    @PositiveOrZero(message = "Giá khuyến mãi phải lớn hơn hoặc bằng 0")
    @Column(name = "sale_price")
    private Double salePrice; // Giá khuyến mãi trực tuyến

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    @Column(nullable = false)
    private Integer stock; // Tồn kho thực tế

    @Size(max = 255, message = "Tên thương hiệu không được vượt quá 255 ký tự")
    @Column(columnDefinition = "NVARCHAR(255)")
    private String brand; // Thương hiệu (Ví dụ: Lani Farm, Dalat Coffee...)

    @NotNull(message = "Danh mục không được để trống")
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Category category;

    @Column(name = "image_url")
    private String imageUrl; // Ảnh đại diện chính của sản phẩm

    @Column(nullable = false)
    private Boolean available = true;
}
